package fr.ujm.tse.lt2c.satin.reasoner;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
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
import fr.ujm.tse.lt2c.satin.dictionary.AbstractDictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.rules.ReasonerProfile;
import fr.ujm.tse.lt2c.satin.rules.Rule;
import fr.ujm.tse.lt2c.satin.rules.run.AvaibleRuns;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;

/**
 * 
 * 
 * @author Jules Chevalier
 */
public class ReasonerStreamed extends Thread {

    private static final Logger LOGGER = Logger.getLogger(ReasonerStreamed.class);

    private int maxThreads = 0;
    private int bufferSize = 100000;
    private final ReasonerProfile profile;
    private final ExecutorService executor;
    private final TripleStore tripleStore;
    private final Dictionary dictionary;
    private final TripleManager tripleManager;
    private final AtomicInteger phaser;
    private boolean running = true;

    /**
     * Constructors
     */
    public ReasonerStreamed(final TripleStore tripleStore, final Dictionary dictionary, final ReasonerProfile profile, final long timeout) {
        super();
        this.tripleStore = tripleStore;
        this.dictionary = dictionary;
        this.profile = profile;
        this.tripleManager = new TripleManager(timeout);
        this.phaser = new AtomicInteger();
        this.executor = Executors.newCachedThreadPool();

        /* Initialize rules used for inference on RhoDF */
        this.initialiseReasoner(this.profile, this.tripleStore, this.dictionary, this.tripleManager, this.phaser, this.executor);

    }

    /*
     * Constructor with configuration
     */
    public ReasonerStreamed(final TripleStore tripleStore, final Dictionary dictionary, final ReasonerProfile profile, final int bufferSize,
            final int maxThreads, final long timeout) {
        super();
        this.tripleStore = tripleStore;
        this.dictionary = dictionary;
        this.profile = profile;
        this.tripleManager = new TripleManager(timeout);
        this.phaser = new AtomicInteger();
        this.bufferSize = bufferSize;
        this.maxThreads = maxThreads;
        this.executor = Executors.newFixedThreadPool(maxThreads);

        /* Initialize rules used for inference on RhoDF */
        this.initialiseReasoner(this.profile, this.tripleStore, this.dictionary, this.tripleManager, this.phaser, this.executor);
    }

    public void addTriples(final Collection<Triple> triples) {
        this.tripleManager.addTriples(triples);
    }

    public void addTriple(final Triple triple) {
        this.tripleManager.addTriple(triple);
    }

    @Override
    public void run() {

        // this.running = true;
        this.tripleManager.start();
        long nonEmptyBuffers = this.tripleManager.flushBuffers();

        while (this.running || nonEmptyBuffers > 0) {

            synchronized (this.phaser) {
                long stillRunnning = this.phaser.get();
                while (stillRunnning > 0) {
                    try {
                        this.phaser.wait();
                    } catch (final InterruptedException e) {
                        LOGGER.error("", e);
                    }
                    stillRunnning = this.phaser.get();
                }
            }

            nonEmptyBuffers = this.tripleManager.flushBuffers();

        }

        this.tripleManager.stop();
        shutdownAndAwaitTermination(this.executor);

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
        case BRHODF:
            tripleManager.addRule(AvaibleRuns.RHODF6a, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.RHODF6b, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.RHODF6d, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.RHODF7a, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.RHODF7b, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);

            final long subPropertyOf = AbstractDictionary.subPropertyOf;
            final long subClassOf = AbstractDictionary.subClassOf;
            final long domain = AbstractDictionary.domain;
            final long range = AbstractDictionary.range;
            final long type = AbstractDictionary.type;
            tripleStore.add(new ImmutableTriple(subPropertyOf, subPropertyOf, subPropertyOf));
            tripleStore.add(new ImmutableTriple(subClassOf, subPropertyOf, subClassOf));
            tripleStore.add(new ImmutableTriple(domain, subPropertyOf, domain));
            tripleStore.add(new ImmutableTriple(range, subPropertyOf, range));
            tripleStore.add(new ImmutableTriple(type, subPropertyOf, type));
        case RHODF:
            tripleManager.addRule(AvaibleRuns.CAX_SCO, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.PRP_DOM, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.PRP_RNG, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.PRP_SPO1, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.SCM_DOM2, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.SCM_RNG2, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.SCM_SCO, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.SCM_SPO, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            break;
        case BRDFS:
            tripleManager.addRule(AvaibleRuns.RDFS6, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.RDFS10, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
        case RDFS:
            tripleManager.addRule(AvaibleRuns.CAX_SCO, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.PRP_DOM, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.PRP_RNG, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.PRP_SPO1, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.SCM_DOM1, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.SCM_DOM2, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.SCM_RNG1, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.SCM_RNG2, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.SCM_SCO, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.SCM_SPO, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.RDFS4, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.RDFS8, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.RDFS12, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            tripleManager.addRule(AvaibleRuns.RDFS13, executor, phaser, dictionary, tripleStore, this.bufferSize, this.maxThreads);
            break;
        default:
            LOGGER.error("Reasoner profile unknown: " + profile);
            break;
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

    public void close() {
        this.running = false;
    }

    public Collection<Rule> getRules() {
        return this.tripleManager.getRules();
    }
}
