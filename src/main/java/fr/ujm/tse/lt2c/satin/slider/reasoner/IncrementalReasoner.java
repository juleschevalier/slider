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
public class IncrementalReasoner {

    private static final Logger LOGGER = Logger.getLogger(IncrementalReasoner.class);

    public static final int DEFAULT_THREADS_NB = 0;
    public static final Ruleset DEFAULT_PROFILE = Ruleset.RHODF;

    private final int maxThreads;
    private final int bufferSize;
    private final long timeout;
    private final Ruleset profile;
    private final ExecutorService executor;
    private final TripleStore tripleStore;
    private final Dictionary dictionary;
    private final TripleManager tripleManager;
    private final AtomicInteger phaser;

    /**
     * Constructor
     * 
     * @param tripleStore
     * @param dictionary
     * @param profile
     */
    public IncrementalReasoner(final TripleStore tripleStore, final Dictionary dictionary, final Ruleset profile) {
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
    public IncrementalReasoner(final TripleStore tripleStore, final Dictionary dictionary, final Ruleset profile, final int maxThreads, final int bufferSize, final long timeout,
            final long[] predicatesToFavorise) {
        super();
        this.maxThreads = maxThreads;
        this.bufferSize = bufferSize;
        this.timeout = timeout;
        this.profile = profile;
        this.tripleStore = tripleStore;
        this.dictionary = dictionary;

        this.tripleManager = new TripleManager(this.timeout, predicatesToFavorise);
        this.phaser = new AtomicInteger();
        if (this.maxThreads == 0) {
            this.executor = Executors.newCachedThreadPool();
        } else {
            this.executor = Executors.newFixedThreadPool(this.maxThreads);
        }

        /* Initialize rules used for inference on the defined fragment */
        this.initialiseReasoner();
    }

    public IncrementalReasoner(final TripleStore tripleStore, final Dictionary dictionary, final Ruleset profile, final int maxThreads, final int bufferSize, final long timeout) {
        this(tripleStore, dictionary, profile, maxThreads, bufferSize, timeout, null);
    }

    public void addTriples(final Collection<Triple> triples) {
        this.tripleManager.addTriples(triples);
    }

    public void addTriple(final Triple triple) {
        this.tripleManager.addTriple(triple);
    }

    public void start() {

        this.tripleManager.start();

    }

    /**
     * Creates and add rules to the reasoner according to the runs list
     * 
     * @param runs
     */
    private void setRuleset(final Rule[] runs) {
        for (final Rule run : runs) {
            this.tripleManager.addRule(run, this.executor, this.phaser, this.dictionary, this.tripleStore, this.bufferSize, this.maxThreads);
        }
    }

    /*
     * !!!! What about multiple setRuleset ????
     */

    /**
     * Creates and add rules to the reasoner according to the fragment for 1st pass
     * 
     */
    private void initialiseReasoner() {
        switch (this.profile) {
            case BRHODF:
                this.setRuleset(new Rule[] { Rule.CAX_SCO, Rule.PRP_DOM, Rule.PRP_RNG, Rule.PRP_SPO1, Rule.SCM_DOM2, Rule.SCM_RNG2, Rule.SCM_SCO, Rule.SCM_SPO, Rule.RHODF6a,
                        Rule.RHODF6b, Rule.RHODF6d, Rule.RHODF7a, Rule.RHODF7b });
                break;
            case RHODF:
                this.setRuleset(new Rule[] { Rule.CAX_SCO, Rule.PRP_DOM, Rule.PRP_RNG, Rule.PRP_SPO1, Rule.SCM_DOM2, Rule.SCM_RNG2, Rule.SCM_SCO, Rule.SCM_SPO });
                break;
            case BRDFS:
                this.setRuleset(new Rule[] { Rule.CAX_SCO, Rule.PRP_DOM, Rule.PRP_RNG, Rule.PRP_SPO1, Rule.SCM_DOM1, Rule.SCM_DOM2, Rule.SCM_RNG1, Rule.SCM_RNG2, Rule.SCM_SCO,
                        Rule.SCM_SPO, Rule.RDFS4, Rule.RDFS8, Rule.RDFS12, Rule.RDFS13, Rule.RDFS6, Rule.RDFS10 });
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
                break;
            case RDFS:
                this.setRuleset(new Rule[] { Rule.CAX_SCO, Rule.PRP_DOM, Rule.PRP_RNG, Rule.PRP_SPO1, Rule.SCM_DOM1, Rule.SCM_DOM2, Rule.SCM_RNG1, Rule.SCM_RNG2, Rule.SCM_SCO,
                        Rule.SCM_SPO, Rule.RDFS4, Rule.RDFS8, Rule.RDFS12, Rule.RDFS13 });
                break;
            case LRDFS:
                this.setRuleset(new Rule[] { Rule.CAX_SCO, Rule.PRP_DOM, Rule.PRP_RNG, Rule.PRP_SPO1, Rule.SCM_DOM1, Rule.SCM_DOM2, Rule.SCM_RNG1, Rule.SCM_RNG2, Rule.SCM_SCO,
                        Rule.SCM_SPO });
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

    /**
     * Waits for the end of the inference and stops reasoner's threads
     */
    public void closeAndWait() {
        this.waitFixPoint();
        this.tripleManager.stop();
        // this.waitFixPoint();
        shutdownAndAwaitTermination(this.executor);
    }

    public void waitFixPoint() {
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
