package fr.ujm.tse.lt2c.satin.reasoner;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import fr.ujm.tse.lt2c.satin.buffer.TripleManager;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.main.Main;
import fr.ujm.tse.lt2c.satin.rules.ReasonerProfile;
import fr.ujm.tse.lt2c.satin.rules.Rule;
import fr.ujm.tse.lt2c.satin.rules.run.AvaibleRuns;
import fr.ujm.tse.lt2c.satin.rules.run.RunFinalizer;
import fr.ujm.tse.lt2c.satin.utils.GlobalValues;
import fr.ujm.tse.lt2c.satin.utils.ParserImplNaive;
import fr.ujm.tse.lt2c.satin.utils.ReasoningArguments;
import fr.ujm.tse.lt2c.satin.utils.RunEntity;

/**
 * 
 * 
 * @author Jules Chevalier
 */
public class ReasonerStreamed {

    private static final Logger LOGGER = Logger.getLogger(ReasonerStreamed.class);

    private static final int SESSION_ID = UUID.randomUUID().hashCode();
    private static final int AVAILABLE_CORES = Runtime.getRuntime().availableProcessors();

    private int maxThreads = AVAILABLE_CORES;
    private final int bufferSize;
    private final ReasonerProfile profile;
    private static ExecutorService executor;
    private final TripleStore tripleStore;
    private final Dictionary dictionary;

    /*
     * Static fields for logging
     */
    private static String machine;
    private static long ram;
    static {
        machine = "";
        try {
            machine = InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException e) {
            LOGGER.error("", e);
        }
        machine += " " + System.getProperty("os.name") + " " + System.getProperty("os.version") + "(" + System.getProperty("os.arch") + ")";
        ram = Runtime.getRuntime().totalMemory();
    }

    /**
     * Constructors
     */
    public ReasonerStreamed(final TripleStore tripleStore, final Dictionary dictionary, final ReasonerProfile profile, final int threadsPerCore,
            final int bufferSize) {
        super();
        this.tripleStore = tripleStore;
        this.dictionary = dictionary;
        this.profile = profile;
        this.maxThreads = AVAILABLE_CORES * threadsPerCore;
        this.bufferSize = bufferSize;
    }

    public ReasonerStreamed(final TripleStore tripleStore, final Dictionary dictionary, final ReasoningArguments arguments) {
        this(tripleStore, dictionary, arguments.getProfile(), arguments.getThreadsPerCore(), arguments.getBufferSize());
    }

    public RunEntity infereFromFile(final String input) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("*****************************");
            LOGGER.debug("Inference from file " + input);
        }

        /* File parsing */
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Parsing");
        }
        long debugParsingTime = System.nanoTime();
        final Parser parser = new ParserImplNaive(this.dictionary, this.tripleStore);
        parser.parse(input);
        debugParsingTime = System.nanoTime() - debugParsingTime;

        /* Inference */
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Inference");
        }
        return this.infere(input, debugParsingTime);

    }

    public RunEntity infereFromModel(final Model model) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("*****************************");
            LOGGER.debug("Inference from Model " + model.toString());
        }

        /* Model parsing */
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Parsing");
        }
        long debugParsingTime = System.nanoTime();
        final Parser parser = new ParserImplNaive(this.dictionary, this.tripleStore);
        parser.parse(model);
        debugParsingTime = System.nanoTime() - debugParsingTime;

        /* Inference */
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Inference");
        }
        return this.infere("Model", debugParsingTime);

    }

    private RunEntity infere(final String input, final long debugParsingTime) {

        /* Initialize structures */
        final TripleManager tripleManager = new TripleManager();
        final AtomicInteger phaser = new AtomicInteger();
        if (this.maxThreads > 0) {
            executor = Executors.newFixedThreadPool(this.maxThreads);
        } else {
            executor = Executors.newCachedThreadPool();
        }
        final ThreadPoolExecutor cpe = (ThreadPoolExecutor) executor;

        final long debugBeginNbTriples = this.tripleStore.size();

        /* Initialize rules used for inference on RhoDF */
        this.initialiseReasoner(this.profile, this.tripleStore, this.dictionary, tripleManager, phaser, executor);

        /********************
         * LAUNCH INFERENCE *
         ********************/

        final long debugStartTime = System.nanoTime();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Send all triples for inference");
        }
        // TODO And what if there already had some triples before ?
        tripleManager.addTriples(this.tripleStore.getAll());

        /*
         * Notify the triple manager that we don't have more triples and wait
         * the end
         */

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Done.");
            LOGGER.debug("Notify the buffers to flush last triples");
        }
        long nonEmptyBuffers = tripleManager.flushBuffers();
        while (nonEmptyBuffers > 0) {

            synchronized (phaser) {
                long stillRunnning = phaser.get();
                while (stillRunnning > 0) {
                    try {
                        phaser.wait();
                    } catch (final InterruptedException e) {
                        LOGGER.error("", e);
                    }
                    stillRunnning = phaser.get();
                }
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Notify the buffers to flush last triples");
            }
            nonEmptyBuffers = tripleManager.flushBuffers();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Notify the buffers to flush last triples " + nonEmptyBuffers);
            }

        }

        /* Reasoning must be ended */

        /* Infer last triples */
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Infere last triples (finalization)");
        }
        this.infereLastTriples(this.profile, this.tripleStore, this.dictionary, phaser);

        shutdownAndAwaitTermination(executor);

        /*****************
         * END INFERENCE *
         *****************/

        final long debugEndTime = System.nanoTime();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Infere over *****************");
        }

        LOGGER.info("largest:" + cpe.getLargestPoolSize() + " maximum:" + cpe.getMaximumPoolSize() + " count:" + cpe.getTaskCount());

        /*
         * Some information display
         */

        /* Make runEntity */
        final RunEntity runEntity = new RunEntity(machine, AVAILABLE_CORES, ram, this.maxThreads, this.bufferSize, "Stream", this.profile.name(), SESSION_ID,
                input, new Date(), debugParsingTime, debugEndTime - debugStartTime, debugBeginNbTriples, this.tripleStore.size() - debugBeginNbTriples, "",
                GlobalValues.getRunsByRule(), GlobalValues.getDuplicatesByRule(), GlobalValues.getInferedByRule());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Input: " + input);
            LOGGER.debug("Parsing: " + debugBeginNbTriples + " in " + Main.nsToTime(debugParsingTime));
        }
        if (LOGGER.isInfoEnabled()) {

            LOGGER.info(input.split("/")[input.split("/").length - 1] + ": " + debugBeginNbTriples + " -> " + this.tripleStore.size() + "(+"
                    + (this.tripleStore.size() - debugBeginNbTriples) + ") in " + Main.nsToTime(debugEndTime - debugStartTime) + "("
                    + Main.nsToTime(debugParsingTime) + ")");

        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Options: profile " + this.profile + ", Threads " + this.maxThreads + ", Buffer " + this.bufferSize);
            LOGGER.debug("Runs by Rule: " + GlobalValues.getRunsByRule());
            LOGGER.debug("Duplicates by Rule: " + GlobalValues.getDuplicatesByRule());
            LOGGER.debug("Infered by Rule: " + GlobalValues.getInferedByRule());

        }
        GlobalValues.addTimeForFile(input, debugEndTime - debugStartTime);

        // LOGGER.info(runEntity);

        return runEntity;
    }

    /**
     * Creates and add rules to the reasoner according to the fragment
     * 
     * @param profile
     * @param tripleStore
     * @param dictionary
     * @param tripleManager
     * @param phaser
     * @param executor
     */
    private void initialiseReasoner(final ReasonerProfile profile, final TripleStore tripleStore, final Dictionary dictionary,
            final TripleManager tripleManager, final AtomicInteger phaser, final ExecutorService executor) {
        switch (profile) {
        case GRHODF:
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
        case RHODF:
            tripleManager.addRule(new Rule(AvaibleRuns.CAX_SCO, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.PRP_DOM, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.PRP_RNG, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.PRP_SPO1, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_DOM2, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_RNG2, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_SCO, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_SPO, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            break;
        case RHODFPP:
            tripleManager.addRule(new Rule(AvaibleRuns.CAX_SCO, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.PRP_DOM, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.PRP_RNG, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.PRP_SPO1, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_DOM2, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_RNG2, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_SCO, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_SPO, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            break;
        case RDFS:
            tripleManager.addRule(new Rule(AvaibleRuns.CAX_SCO, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.PRP_DOM, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.PRP_RNG, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.PRP_SPO1, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_DOM1, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_DOM2, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_RNG1, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_RNG2, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_SCO, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_SPO, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.RDFS4, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.RDFS8, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.RDFS12, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.RDFS13, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            break;
        default:
            LOGGER.error("Reasoner profile unknown: " + profile);
            break;
        }

    }

    /**
     * Infers the last triples which won't throw new rules
     * 
     * @param profile
     * @param tripleStore
     * @param dictionary
     * @param phaser
     */
    private void infereLastTriples(final ReasonerProfile profile, final TripleStore tripleStore, final Dictionary dictionary, final AtomicInteger phaser) {
        final RunFinalizer finalizer = new RunFinalizer(tripleStore, dictionary, profile, executor, phaser, this.bufferSize);
        if (finalizer.isUseful()) {
            finalizer.addTriples(tripleStore.getAll());
            finalizer.clearBuffer();

            synchronized (phaser) {
                long running = phaser.get();
                while (running > 0) {
                    try {
                        phaser.wait();
                    } catch (final InterruptedException e1) {
                        LOGGER.error("", e1);
                    }
                    running = phaser.get();
                }
            }
        }

    }

    /**
     * Add the tripleStore into Jena Model and use Jena Dumper to write it in a file
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
            LOGGER.error("", e);
        } catch (final IOException e) {
            LOGGER.error("", e);
        }
    }

    /**
     * Properly terminates an executor service
     * 
     * @param pool
     */
    static void shutdownAndAwaitTermination(final ExecutorService pool) {
        // Disable new tasks from being submitted
        pool.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(50, TimeUnit.MILLISECONDS)) {
                // Cancel currently executing tasks
                pool.shutdownNow();
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(50, TimeUnit.MILLISECONDS)) {
                    LOGGER.error("Pool did not terminate");
                }
            }
        } catch (final InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            LOGGER.error("", ie);
            pool.shutdownNow();

            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
