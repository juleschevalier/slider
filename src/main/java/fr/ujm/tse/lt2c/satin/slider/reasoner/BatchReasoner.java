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

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.slider.buffer.BufferTimer;
import fr.ujm.tse.lt2c.satin.slider.buffer.QueuedTripleBufferLock;
import fr.ujm.tse.lt2c.satin.slider.buffer.TripleManager;
import fr.ujm.tse.lt2c.satin.slider.dictionary.AbstractDictionary;
import fr.ujm.tse.lt2c.satin.slider.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.slider.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.slider.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.slider.rules.RuleModule;
import fr.ujm.tse.lt2c.satin.slider.rules.Ruleset;
import fr.ujm.tse.lt2c.satin.slider.rules.run.Rule;
import fr.ujm.tse.lt2c.satin.slider.triplestore.ImmutableTriple;

/**
 * 
 * 
 * @author Jules Chevalier
 */
public class BatchReasoner {

    private static final Logger LOGGER = Logger.getLogger(BatchReasoner.class);

    public static final int DEFAULT_THREADS_NB = 0;
    public static final Ruleset DEFAULT_PROFILE = Ruleset.RHODF;

    private final int maxThreads;
    private final int bufferSize;
    private final long timeout;
    private final Ruleset profile;
    private final ExecutorService executor;
    private final TripleStore tripleStore;
    private final Dictionary dictionary;
    private TripleManager tripleManager;
    private final AtomicInteger phaser;

    /**
     * Constructor
     * 
     * @param tripleStore
     * @param dictionary
     * @param profile
     */
    public BatchReasoner(final TripleStore tripleStore, final Dictionary dictionary, final Ruleset profile) {
        this(tripleStore, dictionary, profile, DEFAULT_THREADS_NB, QueuedTripleBufferLock.DEFAULT_BUFFER_SIZE, BufferTimer.DEFAULT_TIMEOUT);
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
    public BatchReasoner(final TripleStore tripleStore, final Dictionary dictionary, final Ruleset profile, final int maxThreads, final int bufferSize, final long timeout) {
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

    public void reasonOn(final Collection<Triple> triples) {

        this.tripleManager.start();
        this.tripleManager.addTriples(triples);
        this.waitFixPoint();
        this.tripleManager.stop();

        this.tripleManager = new TripleManager(this.timeout);
        this.tripleManager.start();
        this.initialiseReasoner2ndPass();
        if (this.tripleManager.getRules().size() > 0) {
            this.tripleManager.addTriples(this.tripleStore.getAll());
            this.waitFixPoint();
        }
        this.tripleManager.stop();

        shutdownAndAwaitTermination(this.executor);
    }

    /**
     * Creates and add rules to the reasoner according to the fragment for 1st pass
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
            case RHODF:
                this.tripleManager.addRule(Rule.CAX_SCO, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.PRP_DOM, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.PRP_RNG, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.PRP_SPO1, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.SCM_DOM2, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.SCM_RNG2, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.SCM_SCO, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.SCM_SPO, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                break;
            case BRDFS:
            case RDFS:
            case LRDFS:
                this.tripleManager.addRule(Rule.CAX_SCO, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.PRP_DOM, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.PRP_RNG, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.PRP_SPO1, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.SCM_DOM1, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.SCM_DOM2, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.SCM_RNG1, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.SCM_RNG2, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.SCM_SCO, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.SCM_SPO, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                break;
            default:
                LOGGER.error("Reasoner profile unknown: " + this.profile);
                break;
        }

    }

    /**
     * Creates and add rules to the reasoner according to the fragment for 2nd pass
     * 
     * @param profile
     * @param tripleStore
     * @param dictionary
     * @param tripleManager
     * @param phaser
     * @param executor
     */
    private void initialiseReasoner2ndPass() {
        switch (this.profile) {
            case BRHODF:
                this.tripleManager.addRule(Rule.RHODF6a, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.RHODF6b, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.RHODF6d, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.RHODF7a, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.RHODF7b, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
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
                break;
            case BRDFS:
                this.tripleManager.addRule(Rule.RDFS6, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.RDFS10, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            case RDFS:
                this.tripleManager.addRule(Rule.RDFS4, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.RDFS8, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.RDFS12, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
                this.tripleManager.addRule(Rule.RDFS13, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
            case LRDFS:
                break;
            default:
                LOGGER.error("Reasoner profile unknown: " + this.profile);
                break;
        }

    }

    /**
     * Add the tripleStore into Jena Model and use Jena Dumper to write it in a file
     */
    protected static void outputToFile(final TripleStore tripleStore, final Dictionary dictionary, final String ouputFile) {
        tripleStore.writeToFile(ouputFile, dictionary);
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

    private void waitFixPoint() {
        long nonEmptyBuffers = this.tripleManager.nonEmptyBuffers();
        while (nonEmptyBuffers > 0) {
            /* Waits for parsing */
            synchronized (this.dictionary) {
                try {
                    this.dictionary.wait();
                } catch (final InterruptedException e) {
                    LOGGER.error("", e);
                }
            }
            /* Waits for last running rule */
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
            nonEmptyBuffers = this.tripleManager.nonEmptyBuffers() + this.phaser.get();
        }
    }

    public Collection<RuleModule> getRules() {
        return this.tripleManager.getRules();
    }
}
