package fr.ujm.tse.lt2c.satin.slider.rules.run;

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
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.slider.buffer.BufferTimer;
import fr.ujm.tse.lt2c.satin.slider.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.slider.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.slider.interfaces.RuleRun;
import fr.ujm.tse.lt2c.satin.slider.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.slider.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.slider.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.slider.utils.GlobalValues;

/**
 * @author Jules Chevalier
 *
 */
public abstract class AbstractRun implements RuleRun {

    private static Logger logger = Logger.getLogger(AbstractRun.class);

    protected final Dictionary dictionary;
    protected final TripleStore tripleStore;
    protected final TripleDistributor distributor;
    protected final TripleBuffer tripleBuffer;
    protected String ruleName = "";
    protected final AtomicInteger phaser;
    protected byte complexity = 2;
    private long triplesToRead;
    private BufferTimer timer;

    /**
     * Constructor
     * 
     * @param dictionary
     * @param tripleStore
     * @param tripleBuffer
     * @param tripleDistributor
     * @param phaser
     * 
     * @see Dictionary
     * @see TripleStore
     */
    public AbstractRun(final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer, final TripleDistributor tripleDistributor,
            final AtomicInteger phaser) {
        this.dictionary = dictionary;
        this.tripleStore = tripleStore;
        this.distributor = tripleDistributor;
        this.tripleBuffer = tripleBuffer;
        this.phaser = phaser;
        this.timer = null;
        this.triplesToRead = 0;
    }

    @Override
    public void run() {

        /*
         * Buffer verification
         */

        if (this.tripleBuffer.size() == 0) {
            synchronized (this.phaser) {
                this.phaser.decrementAndGet();
                this.phaser.notifyAll();
            }

            return;
        }

        try {

            /*
             * Get triples from buffer
             */
            final TripleStore usableTriples;
            if (this.triplesToRead > 0) {
                usableTriples = this.tripleBuffer.clear(this.triplesToRead);
            } else {
                usableTriples = this.tripleBuffer.clear();

            }

            if (usableTriples == null) {
                synchronized (this.phaser) {
                    this.phaser.decrementAndGet();
                    this.phaser.notifyAll();
                }
                return;
            }

            GlobalValues.incRunsByRule(this.ruleName);

            /*
             * Initialize structure and get new triples from process()
             */
            final Collection<Triple> outputTriples = new HashSet<>();

            /* For rules with 2 components */
            if (!usableTriples.isEmpty()) {
                if (this.complexity == 2) {
                    this.process(usableTriples, this.tripleStore, outputTriples);
                    this.process(this.tripleStore, usableTriples, outputTriples);
                } else if (this.complexity == 1) {
                    this.process(this.tripleStore, usableTriples, outputTriples);
                }
            }

            /*
             * Add new triples to the TripleStore
             */
            this.addNewTriples(outputTriples);

        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            /*
             * Unregister from phaser and notifies the Reasoner
             */
            synchronized (this.phaser) {
                this.phaser.decrementAndGet();
                this.phaser.notifyAll();
            }
            if (this.triplesToRead > 0) {
                // System.out.println("Run done for " + this.triplesToRead + "triples " + this.ruleName);
                this.timer.deactivateRule(this.ruleName);
            } else {
                // System.out.println("Run done for " + this.ruleName);
            }

        }
    }

    protected abstract int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples);

    protected void addNewTriples(final Collection<Triple> outputTriples) {

        int duplicates = 0;

        if (outputTriples.isEmpty()) {
            return;
        }

        final Collection<Triple> newTriples = this.tripleStore.addAll(outputTriples);

        duplicates = outputTriples.size() - newTriples.size();
        GlobalValues.incInferedByRule(this.ruleName, newTriples.size());
        GlobalValues.incDuplicatesByRule(this.ruleName, duplicates);

        this.distributor.distributeAll(newTriples);

        return;
    }

    public String getRuleName() {
        return this.ruleName;
    }

    public TripleBuffer getTripleBuffer() {
        return this.tripleBuffer;
    }

    public TripleDistributor getDistributor() {
        return this.distributor;
    }

    @Override
    public String toString() {
        return this.ruleName;
    }

    public AtomicInteger getPhaser() {
        return this.phaser;
    }

    public void setTimerCall(final BufferTimer timer, final long triplesToRead) {
        this.timer = timer;
        this.triplesToRead = triplesToRead;
    }

}
