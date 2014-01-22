package fr.ujm.tse.lt2c.satin.reasoner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.buffer.TripleManager;
import fr.ujm.tse.lt2c.satin.dictionary.DictionaryPrimitrivesRWLock;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.rules.Rule;
import fr.ujm.tse.lt2c.satin.rules.run.RunSCM_SCO;
import fr.ujm.tse.lt2c.satin.tools.Comparator;
import fr.ujm.tse.lt2c.satin.tools.ParserImplNaive;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

/**
 * 
 * @author Jules Chevalier
 */
public class ReasonnerStreamed {

    private static final Logger logger = Logger.getLogger(ReasonnerStreamed.class);
    public static final int SESSION_ID = UUID.randomUUID().hashCode();
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

    public static void main(String[] args) {

        long fail = 0;

        List<String> files = new ArrayList<String>();

        // files.add("tiny_subclassof.owl");
        files.add("subclassof.owl");
        // files.add("sample1.owl");
        // files.add("univ-bench.owl");
        // files.add("sweetAll.owl");
        // files.add("wine.rdf");
        // files.add("geopolitical_200Ko.owl");
        // files.add("geopolitical_300Ko.owl");
        // files.add("geopolitical_500Ko.owl");
        // files.add("geopolitical_1Mo.owl");
        // files.add("geopolitical.owl");
        // files.add("efo.owl");
        // files.add("opencyc.owl");

        try {

            int max = 10;

            for (int file = 0; file < files.size(); file++) {

                for (int i = 0; i < max; i++) {

                    if (!infere(files.get(file))) {
                        fail++;
                    }
                }
            }
            if (PERSIST_RESULTS) {
                if (logger.isInfoEnabled()) {
                    logger.info("SESSION ID : " + SESSION_ID);
                }
            }
            if (fail > 0) {
                logger.warn("FAIL" + (fail > 0 ? "S" : "") + ": " + fail + "(" + ((int) (fail * 100 / max)) + "%)");
            } else {
                logger.info("NO FAIL");
            }
            shutdownAndAwaitTermination(executor);

        } catch (Exception e) {
            logger.error("", e);
        }

    }

    private static boolean infere(String input) {

        boolean success = false;

        logger.debug("********************************NEW RUN********************************");

        /* Initialize structures */
        TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        Dictionary dictionary = new DictionaryPrimitrivesRWLock();
        TripleManager tripleManager = new TripleManager();
        Parser parser = new ParserImplNaive(dictionary, tripleStore);

        Phaser phaser = new Phaser(1) {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                return phase >= 1 || registeredParties == 0;
            }
        };
        // Phaser phaser = new Phaser(1);
        /* File parsing */
        parser.parse(input);

        if (logger.isTraceEnabled()) {
            logger.trace("---DICTIONARY---");
            logger.trace(dictionary.printDico());
            logger.trace("----TRIPLES-----");
            for (Triple triple : tripleStore.getAll()) {
                logger.trace(triple + " " + dictionary.printTriple(triple));
            }
            logger.trace("-------------");
        }

        long debugBeginNbTriples = tripleStore.size();

        /* Initialize rules used for inference on RhoDF */

        executor = Executors.newFixedThreadPool(MAX_THREADS);

        // tripleManager.addRule(new Rule(new RunCAX_SCO(dictionary,
        // tripleStore, phaser), executor));
        // tripleManager.addRule(new Rule(new RunPRP_DOM(dictionary,
        // tripleStore, phaser), executor));
        // tripleManager.addRule(new Rule(new RunPRP_RNG(dictionary,
        // tripleStore, phaser), executor));
        // tripleManager.addRule(new Rule(new RunPRP_SPO1(dictionary,
        // tripleStore, phaser), executor));
        tripleManager.addRule(new Rule(new RunSCM_SCO(dictionary, tripleStore, phaser), executor));
        // tripleManager.addRule(new Rule(new RunSCM_EQC2(dictionary,
        // tripleStore, phaser), executor));
        // tripleManager.addRule(new Rule(new RunSCM_SPO(dictionary,
        // tripleStore, phaser), executor));
        // tripleManager.addRule(new Rule(new RunSCM_EQP2(dictionary,
        // tripleStore, phaser), executor));
        // tripleManager.addRule(new Rule(new RunSCM_DOM1(dictionary,
        // tripleStore, phaser), executor));
        // tripleManager.addRule(new Rule(new RunSCM_DOM2(dictionary,
        // tripleStore, phaser), executor));
        // tripleManager.addRule(new Rule(new RunSCM_RNG1(dictionary,
        // tripleStore, phaser), executor));
        // tripleManager.addRule(new Rule(new RunSCM_RNG2(dictionary,
        // tripleStore, phaser), executor));

        if (logger.isDebugEnabled()) {
            logger.debug(tripleManager.getRules().size() + " Rules initialized\n");
        }

        if (logger.isTraceEnabled()) {
            for (Rule rule : tripleManager.getRules()) {
                logger.trace(rule.getTripleDistributor().subscribers(rule.name(), dictionary));
            }
        }

        logger.debug("********************************START INFERENCE********************************");

        long debugStartTime = System.nanoTime();

        /********************
         * LAUNCH INFERENCE *
         ********************/
        tripleManager.addTriples(tripleStore.getAll());

        /*
         * Notify the triple manager that we don't have more triples and wait
         * the end
         */

        long still = 1;
        while (still != 0) {

            if (logger.isTraceEnabled()) {
                logger.trace("REASONNER Finish Them!");
            }

            still = tripleManager.finishThem();

            if (logger.isTraceEnabled()) {
                logger.trace("REASONER There are still " + still + " non empty buffers");
            }

            phaser.arriveAndAwaitAdvance();

            // while (phaser.getUnarrivedParties() > 1) {}

            still = tripleManager.finishThem();

        }

        /* Reasoning must be ended */

        if (tripleManager.finishThem() > 0) {
            logger.error("Well, fuck");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("REASONNER FAtality!");
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            logger.error("", e);
        }

        long debugEndTime = System.nanoTime();

        logger.debug("*********************************END INFERENCE*********************************");

        logger.debug("Reasoning is finished");

        for (Rule rule : tripleManager.getRules()) {
            logger.debug(rule.getRun().getRuleName() + " launched " + rule.getRun().getThreads() + " time" + (rule.getRun().getThreads() > 1 ? "s" : ""));
        }

        /*
         * Some information display
         */

        if (logger.isInfoEnabled()) {

            logger.info("-----------------------------------------");
            logger.info(input + ": " + debugBeginNbTriples + " -> " + (tripleStore.size() - debugBeginNbTriples) + " in " + (TimeUnit.MILLISECONDS.convert(debugEndTime - debugStartTime, TimeUnit.NANOSECONDS)) + " ms");

            Map<Integer, List<String>> diffTriples = Comparator.compare("jena_" + input, dictionary, tripleStore);

            List<String> missingTriples = diffTriples.get(0);
            List<String> tooTriples = diffTriples.get(1);

            // for (Triple t : tripleStore.getAll()) {
            // System.out.println(dictionary.printTriple(t));
            // }
            if (missingTriples.size() + tooTriples.size() == 0) {
                logger.info("Results match");
                success = true;
            } else {
                logger.info("-" + missingTriples.size() + " +" + tooTriples.size());

                // for (String string : missingTriples) {
                // logger.info("- " + string);
                // }
                // for (String string : tooTriples) {
                // logger.info("+ " + string);
                // }
                /* Must disappear */
                // System.exit(-1);
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

    static void shutdownAndAwaitTermination(ExecutorService pool) {
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
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            logger.error("", ie);
            pool.shutdownNow();

            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
