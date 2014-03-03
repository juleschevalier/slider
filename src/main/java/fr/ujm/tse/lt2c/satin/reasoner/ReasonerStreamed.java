package fr.ujm.tse.lt2c.satin.reasoner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
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
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.rules.Rule;
import fr.ujm.tse.lt2c.satin.rules.run.AvaibleRuns;
import fr.ujm.tse.lt2c.satin.rules.run.ReasonerProfile;
import fr.ujm.tse.lt2c.satin.rules.run.RunFinalizer;
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
    private final ReasonerProfile profile;
    private static ExecutorService executor;
    private final TripleStore tripleStore;
    private final Dictionary dictionary;

    /*
     * Constructors
     */
    public ReasonerStreamed(final TripleStore tripleStore, final Dictionary dictionary, final ReasonerProfile profile, final int threadsPerCore,
            final int bufferSize, final long timeout, final boolean cumulativeMode) {
        super();
        this.tripleStore = tripleStore;
        this.dictionary = dictionary;
        this.profile = profile;
        this.threadsPerCore = threadsPerCore;
        this.maxThreads = AVAILABLE_CORES * this.threadsPerCore;
        this.bufferSize = bufferSize;
        this.timeout = timeout;
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
    }

    public RunEntity infereFromFile(final String input) {

        if (logger.isInfoEnabled()) {
            logger.info("** " + (new File(input)).getName() + " **");
        }

        /* File parsing */
        long debugParsingTime = System.nanoTime();
        final Parser parser = new ParserImplNaive(this.dictionary, this.tripleStore);
        parser.parse(input);
        debugParsingTime = System.nanoTime() - debugParsingTime;

        return this.infere(input, debugParsingTime);

    }

    public RunEntity infereFromModel(final Model model) {

        if (logger.isInfoEnabled()) {
            logger.info("** From Model **");
        }

        /* Model parsing */
        long debugParsingTime = System.nanoTime();
        final Parser parser = new ParserImplNaive(this.dictionary, this.tripleStore);
        parser.parse(model);
        debugParsingTime = System.nanoTime() - debugParsingTime;

        return this.infere("Model", debugParsingTime);

    }

    private RunEntity infere(final String input, final long debugParsingTime) {

        if (logger.isDebugEnabled()) {
            logger.debug("********************************NEW RUN********************************");
        }

        /* Initialize structures */

        final TripleManager tripleManager = new TripleManager();
        final AtomicInteger phaser = new AtomicInteger();
        executor = Executors.newFixedThreadPool(this.maxThreads);

        final long debugBeginNbTriples = this.tripleStore.size();

        /* Initialize rules used for inference on RhoDF */

        this.initialiseReasoner(this.profile, this.tripleStore, this.dictionary, tripleManager, phaser, executor);

        if (logger.isDebugEnabled()) {
            logger.debug("********************************START INFERENCE********************************");
        }

        /********************
         * LAUNCH INFERENCE *
         ********************/

        final long debugStartTime = System.nanoTime();

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
                        logger.error("", e);
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

        if ((tripleManager.flushBuffers() > 0) || (phaser.get() > 0)) {
            logger.error("Unfinished business");
        }

        /* Infer last triples */
        this.finalyze(this.profile, this.tripleStore, this.dictionary, phaser);

        if (logger.isDebugEnabled()) {
            logger.debug("REASONNER FAtality!");
        }

        shutdownAndAwaitTermination(executor);

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
            logger.error("", e);
        }
        machine += " " + System.getProperty("os.name") + " " + System.getProperty("os.version") + "(" + System.getProperty("os.arch") + ")";
        final long ram = (((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize());
        final RunEntity runEntity = new RunEntity(machine, AVAILABLE_CORES, ram, this.maxThreads, this.bufferSize, "Stream", this.profile.name(), SESSION_ID,
                input, new Date(), debugParsingTime, (debugEndTime - debugStartTime), debugBeginNbTriples, (this.tripleStore.size() - debugBeginNbTriples), "",
                GlobalValues.getRunsByRule(), GlobalValues.getDuplicatesByRule(), GlobalValues.getInferedByRule());

        if (logger.isInfoEnabled()) {

            logger.info("Inference: " + debugBeginNbTriples + " -> " + this.tripleStore.size() + "(+" + (this.tripleStore.size() - debugBeginNbTriples)
                    + ") in " + (TimeUnit.MILLISECONDS.convert(debugEndTime - debugStartTime, TimeUnit.NANOSECONDS)) + " ms");

        }
        GlobalValues.addTimeForFile(input, (debugEndTime - debugStartTime));

        return runEntity;
    }

    private void initialiseReasoner(final ReasonerProfile profile, final TripleStore tripleStore, final Dictionary dictionary,
            final TripleManager tripleManager, final AtomicInteger phaser, final ExecutorService executor) {
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
        case GRhoDF:
            tripleManager.addRule(new Rule(AvaibleRuns.CAX_SCO, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.PRP_DOM, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.PRP_RNG, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.PRP_SPO1, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
            tripleManager.addRule(new Rule(AvaibleRuns.SCM_DOM2, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads));
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
        // TODO Watch for useless finalizations
        final long goodthings = tripleStore.size();
        final RunFinalizer finalizer = new RunFinalizer(tripleStore, dictionary, profile, executor, phaser, this.bufferSize);
        finalizer.addTriples(tripleStore.getAll());
        finalizer.clearBuffer();

        synchronized (phaser) {
            long running = phaser.get();
            while (running > 0) {
                try {
                    phaser.wait();
                } catch (final InterruptedException e1) {
                    logger.error("", e1);
                }
                running = phaser.get();
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Bullshit stuff : " + (tripleStore.size() - goodthings));
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
            logger.error("", e);
        } catch (final IOException e) {
            logger.error("", e);
        }
    }

    static void shutdownAndAwaitTermination(final ExecutorService pool) {
        // System.exit(-1);
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
