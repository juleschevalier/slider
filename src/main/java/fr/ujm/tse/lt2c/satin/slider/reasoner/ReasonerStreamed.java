package fr.ujm.tse.lt2c.satin.slider.reasoner;

/*
 * #%L
 * SLIDeR
 * %%
 * Copyright (C) 2014 Université Jean Monnet, Saint Etienne
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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

import fr.ujm.tse.lt2c.satin.slider.buffer.BufferTimer;
import fr.ujm.tse.lt2c.satin.slider.buffer.QueuedTripleBufferLock;
import fr.ujm.tse.lt2c.satin.slider.buffer.TripleManager;
import fr.ujm.tse.lt2c.satin.slider.dictionary.AbstractDictionary;
import fr.ujm.tse.lt2c.satin.slider.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.slider.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.slider.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.slider.rules.ReasonerProfile;
import fr.ujm.tse.lt2c.satin.slider.rules.Rule;
import fr.ujm.tse.lt2c.satin.slider.rules.run.AvaibleRuns;
import fr.ujm.tse.lt2c.satin.slider.triplestore.ImmutableTriple;

/**
 * 
 * 
 * @author Jules Chevalier
 */
public class ReasonerStreamed extends Thread {

    private static final Logger LOGGER = Logger.getLogger(ReasonerStreamed.class);

    public static final int DEFAULT_THREADS_NB = 0;
    public static final ReasonerProfile DEFAULT_PROFILE = ReasonerProfile.RHODF;

    private final int maxThreads;
    private final int bufferSize;
    private final long timeout;
    private final ReasonerProfile profile;
    private final ExecutorService executor;
    private final TripleStore tripleStore;
    private final Dictionary dictionary;
    private final TripleManager tripleManager;
    private final AtomicInteger phaser;
    private boolean running = true;

    /**
     * Constructors
     * 
     * @param tripleStore
     * @param dictionary
     * @param profile
     */
    public ReasonerStreamed(final TripleStore tripleStore, final Dictionary dictionary, final ReasonerProfile profile) {
        super();
        this.maxThreads = DEFAULT_THREADS_NB;
        this.bufferSize = QueuedTripleBufferLock.DEFAULT_BUFFER_SIZE;
        this.timeout = BufferTimer.DEFAULT_TIMEOUT;
        this.profile = profile;
        this.tripleStore = tripleStore;
        this.dictionary = dictionary;

        this.tripleManager = new TripleManager(this.timeout);
        this.phaser = new AtomicInteger();
        if (this.maxThreads == 0) {
            this.executor = Executors.newCachedThreadPool();
        } else {
            this.executor = Executors.newFixedThreadPool(this.maxThreads);
        }

        /* Initialize rules used for inference on the defined fragment */
        this.initialiseReasoner();

    }

    /**
     * Constructor
     * 
     * @param maxThreads
     * @param bufferSize
     * @param timeout
     * @param profile
     * @param tripleStore
     * @param dictionary
     */
    public ReasonerStreamed(final TripleStore tripleStore, final Dictionary dictionary, final ReasonerProfile profile, final int maxThreads,
            final int bufferSize, final long timeout) {
        super();
        this.maxThreads = maxThreads;
        this.bufferSize = bufferSize;
        this.timeout = timeout;
        this.profile = profile;
        this.tripleStore = tripleStore;
        this.dictionary = dictionary;

        this.tripleManager = new TripleManager(this.timeout);
        this.phaser = new AtomicInteger();
        if (this.maxThreads == 0) {
            this.executor = Executors.newCachedThreadPool();
        } else {
            this.executor = Executors.newFixedThreadPool(this.maxThreads);
        }

        /* Initialize rules used for inference on the defined fragment */
        this.initialiseReasoner();
    }

    public void addTriples(final Collection<Triple> triples) {
        this.tripleManager.addTriples(triples);
    }

    public void addTriple(final Triple triple) {
        this.tripleManager.addTriple(triple);
    }

    @Override
    public void run() {

        this.tripleManager.start();

        long nonEmptyBuffers = this.tripleManager.nonEmptyBuffers();
        // long nonEmptyBuffers = this.tripleManager.flushBuffers();

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

            nonEmptyBuffers = this.tripleManager.nonEmptyBuffers();
            // nonEmptyBuffers = this.tripleManager.flushBuffers();
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
    private void initialiseReasoner() {
        switch (this.profile) {
        case BRHODF:
            this.tripleManager.addRule(AvaibleRuns.RHODF6a, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.RHODF6b, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.RHODF6d, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.RHODF7a, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.RHODF7b, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);

            /* Axiomatic triples */
            final long subPropertyOf = AbstractDictionary.subPropertyOf;
            final long subClassOf = AbstractDictionary.subClassOf;
            final long domain = AbstractDictionary.domain;
            final long range = AbstractDictionary.range;
            final long type = AbstractDictionary.type;
            this.tripleStore.add(new ImmutableTriple(subPropertyOf, subPropertyOf, subPropertyOf));
            this.tripleStore.add(new ImmutableTriple(subClassOf, subPropertyOf, subClassOf));
            this.tripleStore.add(new ImmutableTriple(domain, subPropertyOf, domain));
            this.tripleStore.add(new ImmutableTriple(range, subPropertyOf, range));
            this.tripleStore.add(new ImmutableTriple(type, subPropertyOf, type));
        case RHODF:
            this.tripleManager.addRule(AvaibleRuns.CAX_SCO, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.PRP_DOM, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.PRP_RNG, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.PRP_SPO1, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.SCM_DOM2, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.SCM_RNG2, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.SCM_SCO, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.SCM_SPO, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            break;
        case BRDFS:
            this.tripleManager.addRule(AvaibleRuns.RDFS4, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.RDFS8, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.RDFS12, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.RDFS13, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.RDFS6, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.RDFS10, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
        case RDFS:
            this.tripleManager.addRule(AvaibleRuns.CAX_SCO, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.PRP_DOM, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.PRP_RNG, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.PRP_SPO1, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.SCM_DOM1, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.SCM_DOM2, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.SCM_RNG1, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.SCM_RNG2, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.SCM_SCO, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            this.tripleManager.addRule(AvaibleRuns.SCM_SPO, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            break;
        default:
            LOGGER.error("Reasoner profile unknown: " + this.profile);
            break;
        }

    }

    // private void initialiseReasoner(final int number) {
    //
    // final List<AvaibleRuns> runs = new ArrayList<AvaibleRuns>();
    //
    // runs.addAll(java.util.Arrays.asList(AvaibleRuns.values()));
    //
    // Collections.shuffle(runs);
    //
    // for (int i = 0; i < number; i++) {
    // this.tripleManager.addRule(runs.get(i), this.executor, this.phaser, this.dictionary, this.tripleStore,
    // this.bufferSize, this.maxThreads);
    // }
    // }

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
