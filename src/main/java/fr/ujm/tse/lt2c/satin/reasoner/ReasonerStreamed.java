package fr.ujm.tse.lt2c.satin.reasoner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import fr.ujm.tse.lt2c.satin.buffer.TripleManager;
import fr.ujm.tse.lt2c.satin.dictionary.DictionaryPrimitrivesRWLock;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.rules.Rule;
import fr.ujm.tse.lt2c.satin.rules.run.AvaibleRuns;
import fr.ujm.tse.lt2c.satin.rules.run.ReasonerProfile;
import fr.ujm.tse.lt2c.satin.rules.run.RunRhoDFFinalizer;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;
import fr.ujm.tse.lt2c.satin.utils.GlobalValues;
import fr.ujm.tse.lt2c.satin.utils.ParserImplNaive;
import fr.ujm.tse.lt2c.satin.utils.ReasoningArguments;
import fr.ujm.tse.lt2c.satin.utils.RunEntity;

/**
 * 
 * @author Jules Chevalier
 */
public class ReasonerStreamed {

    private static final Logger logger = Logger.getLogger(ReasonerStreamed.class);

    private static final int SESSION_ID = UUID.randomUUID().hashCode();
    private static final int AVAILABLE_CORES = Runtime.getRuntime().availableProcessors();

    private final int threadsPerCore;
    private int maxThreads = AVAILABLE_CORES;
    private final int bufferSize;
    private final long timeout;
    private final boolean bullshitMode;
    private final ReasonerProfile profile;
    private static ExecutorService executor;
    private final TripleStore tripleStore;
    private final Dictionary dictionary;

    public static void main(final String[] args) {

        final List<String> files = new ArrayList<String>();

        // files.add("tiny_subclassof.nt");
        // files.add("subclassof.nt");
        // files.add("sample1.nt");
        // files.add("univ-bench.nt");
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
        // files.add("subclassof/subClassOf10.nt");
        // files.add("subclassof/subClassOf50.nt");
        // files.add("subclassof/subClassOf100.nt");
        // files.add("subclassof/subClassOf200.nt");
        // files.add("subclassof/subClassOf500.nt");
        // files.add("subclassof/subClassOf1000.nt");
        files.add("wikipediaOntology.nt");

        try {

            final int max = 1;

            for (int file = 0; file < files.size(); file++) {

                for (int i = 0; i < max; i++) {
                    if (max > 1) {
                        logger.info("Run " + i);
                    }
                    final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
                    final Dictionary dictionary = new DictionaryPrimitrivesRWLock();
                    final ReasonerStreamed reasoner = new ReasonerStreamed(tripleStore, dictionary, ReasonerProfile.RhoDF, 10, 100, 100, false, false);

                    final RunEntity runEntity = reasoner.infere(files.get(file));

                    // Save results in Mongo
                    // if (false) {
                    // MongoClient client;
                    // try {
                    // client = new MongoClient();
                    // final Morphia morphia = new Morphia();
                    // morphia.map(RunEntity.class);
                    // final Datastore ds = morphia.createDatastore(client,
                    // "RunResults");
                    // ds.save(runEntity);
                    //
                    // } catch (final Exception e) {
                    // e.printStackTrace();
                    // }
                }

                // Print TripleStore in n-triples format
                // ReasonnerStreamed.outputToFile(tripleStore, dictionary,
                // "infered_" + files.get(file));
                // if (logger.isInfoEnabled()) {
                // logger.info("Writting to " + "infered_" + files.get(file)
                // + " Ok");
                // }
                // }
            }
            // final Print results
            // if (false) {
            // if (logger.isInfoEnabled()) {
            // logger.info("SESSION ID : " + SESSION_ID);
            // }
            // }
            shutdownAndAwaitTermination(executor);

        } catch (final Exception e) {
            logger.error("", e);
        }

    }

    /*
     * Constructors
     */
    public ReasonerStreamed(final TripleStore tripleStore, final Dictionary dictionary, final ReasonerProfile profile, final int threadsPerCore, final int bufferSize, final long timeout, final boolean bullshitMode, final boolean cumulativeMode) {
        super();
        this.tripleStore = tripleStore;
        this.dictionary = dictionary;
        this.profile = profile;
        this.threadsPerCore = threadsPerCore;
        this.maxThreads = AVAILABLE_CORES * this.threadsPerCore;
        this.bufferSize = bufferSize;
        this.timeout = timeout;
        this.bullshitMode = bullshitMode;
    }

    public ReasonerStreamed(final TripleStore tripleStore, final Dictionary dictionary, final ReasoningArguments arguments) {
        super();
        this.tripleStore = tripleStore;
        this.dictionary = dictionary;
        this.profile = arguments.getProfile();
        this.threadsPerCore = arguments.getThreadsPerCore();
        this.maxThreads = AVAILABLE_CORES * arguments.getThreadsPerCore();
        this.bufferSize = arguments.getBufferSize();
        this.timeout = arguments.getTimeout();
        this.bullshitMode = arguments.isBullshitMode();
    }

    public RunEntity infere(final String input) {

        if (logger.isInfoEnabled()) {
            logger.info("-----------------------------------------");
            logger.info(input);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("********************************NEW RUN********************************");
        }

        /* Initialize structures */

        final TripleManager tripleManager = new TripleManager();
        final Parser parser = new ParserImplNaive(this.dictionary, this.tripleStore);
        final AtomicInteger phaser = new AtomicInteger();
        executor = Executors.newFixedThreadPool(this.maxThreads);

        long debugParsingTime = System.nanoTime();
        /* File parsing */
        parser.parse(input);
        debugParsingTime = System.nanoTime() - debugParsingTime;

        final long debugBeginNbTriples = this.tripleStore.size();

        /* Initialize rules used for inference on RhoDF */

        this.initialiseReasoner(ReasonerProfile.RhoDF, this.tripleStore, this.dictionary, tripleManager, phaser, executor);

        if (logger.isDebugEnabled()) {
            logger.debug("********************************START INFERENCE********************************");
        }

        final long debugStartTime = System.nanoTime();

        /********************
         * LAUNCH INFERENCE *
         ********************/
        tripleManager.addTriples(this.tripleStore.getAll());

        /*
         * Notify the triple manager that we don't have more triples and wait
         * the end
         */

        if (logger.isTraceEnabled()) {
            logger.trace("REASONNER Flush Buffers!");
        }
        long nonEmptyBuffers = tripleManager.flushBuffers();
        while (nonEmptyBuffers > 0) {

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

            if (logger.isTraceEnabled()) {
                logger.trace("REASONNER Flush Buffers!");
            }
            nonEmptyBuffers = tripleManager.flushBuffers();

        }

        /* Reasoning must be ended */

        if (tripleManager.flushBuffers() > 0) {
            logger.error("Non-empty buffers after the end");
        }

        /* Infere last triples */
        if (this.bullshitMode) {
            this.finalyze(ReasonerProfile.GRhoDF, this.tripleStore, this.dictionary, phaser);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("REASONNER FAtality!");
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (final InterruptedException e) {
            logger.error("", e);
        }

        final long debugEndTime = System.nanoTime();

        if (logger.isDebugEnabled()) {
            logger.debug("*********************************END INFERENCE*********************************");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Reasoning is finished");
        }

        /*
         * Some information display
         */

        /* Make runEntity */
        String machine = "";
        try {
            machine = InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException e) {
            e.printStackTrace();
        }
        machine += " " + System.getProperty("os.name") + " " + System.getProperty("os.version") + "(" + System.getProperty("os.arch") + ")";
        final long ram = (((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize());
        final RunEntity runEntity = new RunEntity(machine, AVAILABLE_CORES, ram, this.maxThreads, this.bufferSize, "Stream", SESSION_ID, input, new Date(), debugParsingTime, (debugEndTime - debugStartTime), debugBeginNbTriples, (this.tripleStore.size() - debugBeginNbTriples), "", GlobalValues.getRunsByRule(), GlobalValues.getDuplicatesByRule(), GlobalValues.getInferedByRule());

        if (logger.isInfoEnabled()) {

            logger.info("-------------------");
            logger.info((new File(input)).getName() + ": " + debugBeginNbTriples + " -> " + this.tripleStore.size() + "(+" + (this.tripleStore.size() - debugBeginNbTriples) + ") in " + (TimeUnit.MILLISECONDS.convert(debugEndTime - debugStartTime, TimeUnit.NANOSECONDS)) + " ms");

        }

        return runEntity;

    }

    private void initialiseReasoner(final ReasonerProfile profile, final TripleStore tripleStore, final Dictionary dictionary, final TripleManager tripleManager, final AtomicInteger phaser, final ExecutorService executor) {
        switch (profile) {
        case RhoDF:
            tripleManager.addRule(new Rule(AvaibleRuns.CAX_SCO, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.PRP_DOM, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.PRP_RNG, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.PRP_SPO1, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_DOM1, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_DOM2, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_EQC2, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_EQP2, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_RNG1, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_RNG2, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_SCO, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_SPO, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            break;
        default:
            logger.error("Reasoner profile unknown: " + profile);
            break;
        }

        /* Debugging */
        if (logger.isDebugEnabled()) {
            logger.debug(tripleManager.getRules().size() + " rules initialized\n");
        }

        if (logger.isTraceEnabled()) {
            for (final Rule rule : tripleManager.getRules()) {
                logger.trace(rule.getTripleDistributor().subscribers(rule.name(), dictionary));
            }
        }
    }

    private void finalyze(final ReasonerProfile profile, final TripleStore tripleStore, final Dictionary dictionary, final AtomicInteger phaser) {
        final long goodthings = tripleStore.size();
        final RunRhoDFFinalizer finalizer = new RunRhoDFFinalizer(tripleStore, dictionary, profile, executor, phaser, this.bufferSize);
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

        if (logger.isInfoEnabled()) {
            logger.info("Bullshit stuff : " + (tripleStore.size() - goodthings));
        }
    }

    /**
     * Add into Jena Model and use Jena Dumper
     */
    protected static void outputToFile(final TripleStore tripleStore, final Dictionary dictionary, final String ouputFile) {
        // Create an empty model.
        final Model model = ModelFactory.createDefaultModel();
        // Add all the triples into the model
        for (final Triple triple : tripleStore.getAll()) {
            final Resource subject = ResourceFactory.createResource(dictionary.get(triple.getSubject()));
            final Property predicate = ResourceFactory.createProperty(dictionary.get(triple.getPredicate()));
            final Resource object = ResourceFactory.createResource(dictionary.get(triple.getObject()));
            model.add(subject, predicate, object);
        }
        try {
            final OutputStream os = new FileOutputStream(ouputFile);
            model.write(os, "N-TRIPLES");
            os.close();
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
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

    public static int getMaxThreads() {
        return 0;
    }
}
