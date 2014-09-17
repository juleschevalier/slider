package fr.ujm.tse.lt2c.satin.buffer;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.rules.Rule;
import fr.ujm.tse.lt2c.satin.rules.run.AvaibleRuns;

/**
 * Links all the buffers together
 * Gets new triples and send them to the different rules
 * 
 * @author Jules Chevalier
 */
public class TripleManager {

    private final Logger LOGGER = Logger.getLogger(TripleManager.class);

    private final List<Rule> rules;
    private final TripleDistributor generalDistributor;
    private final Timer timer;
    private final BufferTimer bufferTimer;
    private final long timeout;

    /**
     * Constructor
     * 
     * @param timeout
     */
    public TripleManager(final long timeout) {
        super();
        this.rules = new ArrayList<>();
        this.generalDistributor = new TripleDistributor();
        this.timer = new Timer();
        this.timeout = timeout;
        this.bufferTimer = new BufferTimer(this.timeout);
    }

    public void start() {
        if (this.timeout > 0) {
            for (final Rule rule : this.rules) {
                this.bufferTimer.addRule(rule);
            }
            this.timer.scheduleAtFixedRate(this.bufferTimer, this.timeout, this.timeout);
        }
    }

    public void stop() {
        this.timer.cancel();
    }

    /**
     * Add a rule and connect it with other rules (TripleDistributors with
     * TripleBuffers)
     * 
     * @param run
     * @param executor
     * @param phaser
     * @param dictionary
     * @param tripleStore
     * @param bufferSize
     * @param maxThreads
     * @see Rule
     */
    public void addRule(final AvaibleRuns run, final ExecutorService executor, final AtomicInteger phaser, final Dictionary dictionary,
            final TripleStore tripleStore, final int bufferSize, final int maxThreads) {

        final Rule newRule = new Rule(run, executor, phaser, dictionary, tripleStore, bufferSize, maxThreads, this.bufferTimer);
        this.rules.add(newRule);
        this.generalDistributor.addSubscriber(newRule.getTripleBuffer(), newRule.getInputMatchers());

        for (final Rule rule : this.rules) {
            if (this.match(newRule.getOutputMatchers(), rule.getInputMatchers())) {
                final long[] matchers = this.extractMatchers(newRule.getOutputMatchers(), rule.getInputMatchers());
                newRule.getTripleDistributor().addSubscriber(rule.getTripleBuffer(), matchers);
            }
            if (rule != newRule && this.match(rule.getOutputMatchers(), newRule.getInputMatchers())) {
                rule.getTripleDistributor().addSubscriber(newRule.getTripleBuffer(), newRule.getInputMatchers());
            }
        }
    }

    /**
     * Send new triples to matching rules for inference
     * 
     * @param triples
     */
    public void addTriples(final Collection<Triple> triples) {
        this.generalDistributor.distributeAll(triples);
    }

    /**
     * Send new triple to matching rules for inference
     * 
     * @param triple
     */
    public void addTriple(final Triple triple) {
        this.generalDistributor.distribute(triple);
    }

    /**
     * Used once all triples are sent.
     * Notify any rules with non-empty buffer to stop waiting for new ones and
     * infers on them
     * 
     * @return the number of rules with non-empty buffers
     */
    public long flushBuffers() {
        long total = 0;
        for (final Rule rule : this.rules) {
            if (rule.getTripleBuffer().getOccupation() > 0) {
                total++;
                rule.bufferFull();
            }
        }
        return total;
    }

    /**
     * @return a collection with the Manager rules
     */
    public Collection<Rule> getRules() {
        return this.rules;
    }

    /**
     * Verify if the sets of predicates match
     * 
     * @param in
     * @param out
     * @return true if the two lists have at least one long in common. False
     *         else
     */
    private boolean match(final long[] in, final long[] out) {
        if (in.length == 0 || out.length == 0) {
            return true;
        }
        // Broken loops
        for (final long l1 : out) {
            for (final long l2 : in) {
                if (l1 == l2) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Extract common long between in and out.
     * Used if {@link #match(long[], long[]) is true}
     * 
     * @param out
     * @param in
     * @return the common longs
     * @see #match(long[], long[])
     */
    private long[] extractMatchers(final long[] out, final long[] in) {
        if (in.length == 0 || out.length == 0) {
            return new long[] {};
        }

        final List<Long> matchers = new ArrayList<Long>();
        for (final long l1 : out) {
            for (final long l2 : in) {
                if (l1 == l2) {
                    matchers.add(l1);
                }
            }
        }

        final long[] ms = new long[matchers.size()];

        for (int i = 0; i < matchers.size(); i++) {
            ms[i] = matchers.get(i);
        }

        return ms;
    }

}
