package fr.ujm.tse.lt2c.satin.reasoner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.buffer.TripleManager;
import fr.ujm.tse.lt2c.satin.dictionary.DictionaryPrimitrivesRWLock;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.rules.Rule;
import fr.ujm.tse.lt2c.satin.rules.run.AvaibleRuns;
import fr.ujm.tse.lt2c.satin.rules.run.ReasonerProfiles;
import fr.ujm.tse.lt2c.satin.rules.run.RunRhoDFFinalizer;
import fr.ujm.tse.lt2c.satin.tools.Comparator;
import fr.ujm.tse.lt2c.satin.tools.ParserImplNaive;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

/**
 * 
 * @author Jules Chevalier
 */
public class ReasonnerStreamed {

    private static final Logger logger = Logger.getLogger(ReasonnerStreamed.class);
    private static final int SESSION_ID = UUID.randomUUID().hashCode();
    private static final int AVAILABLE_CORES = Runtime.getRuntime().availableProcessors();
    private static final int THREADS_PER_CORE = 10;
    public static final int MAX_THREADS = AVAILABLE_CORES * THREADS_PER_CORE;
    private static final boolean PERSIST_RESULTS = false;

    private static ExecutorService executor;

    /**
     * Constructor
     */
    private ReasonnerStreamed() {
    }

    public static void main(final String[] args) {

        long fail = 0;

        final List<String> files = new ArrayList<String>();

        // files.add("tiny_subclassof.nt");
        // files.add("subclassof.nt");
        // files.add("sample1.nt");
        files.add("univ-bench.nt");
        // files.add("geopolitical_200Ko.nt");
        // files.add("geopolitical_300Ko.nt");
        // files.add("geopolitical_500Ko.nt");
        // files.add("geopolitical_1Mo.nt");
        // files.add("geopolitical.nt");
        // files.add("dbpedia_3.8.nt");
        // files.add("dataset_100k.nt");
        // files.add("dataset_200k.nt");
        // files.add("dataset_500k.nt");
        // files.add("dataset_1M.nt");
        // files.add("dataset_5M.nt");

        try {

            final int max = 1;

            for (int file = 0; file < files.size(); file++) {

                for (int i = 0; i < max; i++) {
                    if (max > 1) {
                        logger.info(i);
                    }
                    if (!infere(files.get(file))) {
                        fail++;
                        // System.exit(-1);
                    }
                }
            }
            if (PERSIST_RESULTS) {
                if (logger.isInfoEnabled()) {
                    logger.info("SESSION ID : " + SESSION_ID);
                }
            }
            if (fail > 0) {
                logger.warn("FAIL" + (fail > 0 ? "S" : "") + ": " + fail + "(" + ((int) ((fail * 100) / max)) + "%)");
            } else {
                logger.info("NO FAIL");
            }
            shutdownAndAwaitTermination(executor);

        } catch (final Exception e) {
            logger.error("", e);
        }

    }

    private static boolean infere(final String input) {

        boolean success = false;

        if (logger.isDebugEnabled()) {
            logger.debug("********************************NEW RUN********************************");
        }

        /* Initialize structures */
        final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        final Dictionary dictionary = new DictionaryPrimitrivesRWLock();

        final TripleManager tripleManager = new TripleManager();
        final Parser parser = new ParserImplNaive(dictionary, tripleStore);
        final AtomicInteger phaser = new AtomicInteger();
        executor = Executors.newFixedThreadPool(MAX_THREADS);

        /* File parsing */
        parser.parse(input);

        final long debugBeginNbTriples = tripleStore.size();

        /* Initialize rules used for inference on RhoDF */

        initialiseReasoner(tripleStore, dictionary, tripleManager, phaser);

        if (logger.isDebugEnabled()) {
            logger.debug("********************************START INFERENCE********************************");
        }

        final long debugStartTime = System.nanoTime();

        /********************
         * LAUNCH INFERENCE *
         ********************/
        tripleManager.addTriples(tripleStore.getAll());

        /*
         * Notify the triple manager that we don't have more triples and wait
         * the end
         */

        long nonEmptyBuffers = tripleManager.flushBuffers();
        while (nonEmptyBuffers > 0) {

            if (logger.isTraceEnabled()) {
                logger.trace("REASONNER Flush Buffers!");
            }

            if (logger.isTraceEnabled()) {
                logger.trace("REASONER There are still " + nonEmptyBuffers + " non empty buffers");
            }

            synchronized (phaser) {
                long stillRunnning = phaser.get();
                while (stillRunnning > 0) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("REASONER Running rules: " + stillRunnning);
                    }
                    try {
                        phaser.wait();
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                    stillRunnning = phaser.get();
                }
            }

            nonEmptyBuffers = tripleManager.flushBuffers();

        }

        /* Reasoning must be ended */

        if (tripleManager.flushBuffers() > 0) {
            logger.error("Non-empty buffers after the end");
        }

        final long goodthings = tripleStore.size();

        bullshitting(tripleStore, dictionary, phaser);

        if (logger.isDebugEnabled()) {
            logger.debug("REASONNER FAtality!");
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (final InterruptedException e) {
            logger.error("", e);
        }

        if (logger.isInfoEnabled()) {
            logger.info("Bullshit stuff : " + (tripleStore.size() - goodthings));
        }

        final long debugEndTime = System.nanoTime();

        if (logger.isDebugEnabled()) {
            logger.debug("*********************************END INFERENCE*********************************");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Reasoning is finished");
        }

        // for (Rule rule : tripleManager.getRules()) {
        // logger.debug(rule.name() + " launched " + rule.getRun().getThreads()
        // + " time" + (rule.getRun().getThreads() > 1 ? "s" : ""));
        // }

        /*
         * Some information display
         */

        if (logger.isInfoEnabled()) {

            logger.info("-----------------------------------------");
            logger.info(input + ": " + debugBeginNbTriples + " -> " + tripleStore.size() + "(+" + (tripleStore.size() - debugBeginNbTriples) + ") in " + (TimeUnit.MILLISECONDS.convert(debugEndTime - debugStartTime, TimeUnit.NANOSECONDS)) + " ms");

            final Map<Integer, List<String>> diffTriples = Comparator.compare("jena_" + input, dictionary, tripleStore);

            final List<String> missingTriples = diffTriples.get(0);
            final List<String> tooTriples = diffTriples.get(1);

            // for (Triple t : tripleStore.getAll()) {
            // System.out.println(dictionary.printTriple(t));
            // }
            if ((missingTriples.size() + tooTriples.size()) == 0) {
                logger.info("Results match");
                success = true;
            } else {
                logger.info("-" + missingTriples.size() + " +" + tooTriples.size());

                // for (final String string : missingTriples) {
                // logger.info("- " + string);
                // }
                // for (final String string : tooTriples) {
                // logger.info("+ " + string);
                // }
            }

        }

        /*************************
         * MUST SAVE RUN RESULTS *
         *************************/

        if (PERSIST_RESULTS) {
            // TODO Persist run results
        }
        return success;

    }

    private static void initialiseReasoner(final TripleStore tripleStore, final Dictionary dictionary, final TripleManager tripleManager, final AtomicInteger phaser) {

        tripleManager.addRule(new Rule(AvaibleRuns.CAX_SCO, executor, phaser, dictionary, tripleStore));
        tripleManager.addRule(new Rule(AvaibleRuns.PRP_DOM, executor, phaser, dictionary, tripleStore));
        tripleManager.addRule(new Rule(AvaibleRuns.PRP_RNG, executor, phaser, dictionary, tripleStore));
        tripleManager.addRule(new Rule(AvaibleRuns.PRP_SPO1, executor, phaser, dictionary, tripleStore));
        tripleManager.addRule(new Rule(AvaibleRuns.SCM_DOM1, executor, phaser, dictionary, tripleStore));
        tripleManager.addRule(new Rule(AvaibleRuns.SCM_DOM2, executor, phaser, dictionary, tripleStore));
        tripleManager.addRule(new Rule(AvaibleRuns.SCM_EQC2, executor, phaser, dictionary, tripleStore));
        tripleManager.addRule(new Rule(AvaibleRuns.SCM_EQP2, executor, phaser, dictionary, tripleStore));
        tripleManager.addRule(new Rule(AvaibleRuns.SCM_RNG1, executor, phaser, dictionary, tripleStore));
        tripleManager.addRule(new Rule(AvaibleRuns.SCM_RNG2, executor, phaser, dictionary, tripleStore));
        tripleManager.addRule(new Rule(AvaibleRuns.SCM_SCO, executor, phaser, dictionary, tripleStore));
        tripleManager.addRule(new Rule(AvaibleRuns.SCM_SPO, executor, phaser, dictionary, tripleStore));

        if (logger.isDebugEnabled()) {
            logger.debug(tripleManager.getRules().size() + " rules initialized\n");
        }

        if (logger.isTraceEnabled()) {
            for (final Rule rule : tripleManager.getRules()) {
                logger.trace(rule.getTripleDistributor().subscribers(rule.name(), dictionary));
            }
        }
    }

    private static void bullshitting(final TripleStore tripleStore, final Dictionary dictionary, final AtomicInteger phaser) {
        logger.info(dictionary.size());
        final RunRhoDFFinalizer finalizer = new RunRhoDFFinalizer(tripleStore, dictionary, ReasonerProfiles.GRhoDF, executor, phaser);
        finalizer.addTriples(tripleStore.getAll());
        finalizer.clearBuffer();

        synchronized (phaser) {
            long running = phaser.get();
            while (running > 0) {
                try {
                    phaser.wait();
                } catch (final InterruptedException e1) {
                    e1.printStackTrace();
                }
                running = phaser.get();
            }
        }
    }

    static void shutdownAndAwaitTermination(final ExecutorService pool) {
        System.exit(-1);
        // Disable new tasks from being submitted
        pool.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(50, TimeUnit.MILLISECONDS)) {
                // Cancel currently executing tasks
                pool.shutdownNow();
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(50, TimeUnit.MILLISECONDS)) {
                    logger.error("Pool did not terminate");
                }
            }
        } catch (final InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            logger.error("", ie);
            pool.shutdownNow();

            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
