package fr.ujm.tse.lt2c.satin.buffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.rules.Rule;

/**
 * This class is in charge of linking all the buffers together
 * 
 * @author Jules Chevalier
 */
public class TripleManager {

    private static Logger logger = Logger.getLogger(TripleManager.class);

    List<Rule> rules;
    Multimap<String, String> debugLinks;
    TripleDistributor generalDistributor;

    private static final boolean enableTimeout = false;
    private static final long TIMEOUT = 100; // Timeout

    /**
     * Constructor
     */
    public TripleManager() {
        super();
        this.rules = new ArrayList<>();
        debugLinks = HashMultimap.create();
        this.generalDistributor = new TripleDistributor();
        this.generalDistributor.setName("T_MANAGER");
        if (enableTimeout) {
            timeoutChecker();
        }
    }

    /**
     * Add a rule and connect it with other rules (TripleDistributors with
     * TripleBuffers)
     * 
     * @param newRule
     * @see Rule
     */
    public void addRule(Rule newRule) {
        this.rules.add(newRule);
        generalDistributor.addSubscriber(newRule.getTripleBuffer(), newRule.getInputMatchers());
        if (logger.isTraceEnabled()) {
            logger.trace("Triple Manager : -- ADD RULE " + newRule.name() + " --");
            logger.trace("Triple Manager : General -> " + newRule.name());
        }
        for (Rule rule : this.rules) {
            if (match(newRule.getOutputMatchers(), rule.getInputMatchers())) {
                long[] matchers = extractMatchers(newRule.getOutputMatchers(), rule.getInputMatchers());
                newRule.getTripleDistributor().addSubscriber(rule.getTripleBuffer(), matchers);
                debugLinks.put(newRule.name(), rule.name());
                if (logger.isTraceEnabled()) {
                    logger.trace("Triple Manager : " + rule.name() + " -> " + newRule.name());
                }
            }
            if ((rule != newRule) && match(rule.getOutputMatchers(), newRule.getInputMatchers())) {
                rule.getTripleDistributor().addSubscriber(newRule.getTripleBuffer(), newRule.getInputMatchers());
                debugLinks.put(rule.name(), newRule.name());
                if (logger.isTraceEnabled()) {
                    logger.trace("Triple Manager : " + newRule.name() + " -> " + rule.name());
                }
            }
        }
    }

    /**
     * Send new triples to matching rules for inference
     * 
     * @param triples
     */
    public void addTriples(Collection<Triple> triples) {
        this.generalDistributor.distribute(triples);
    }

    /**
     * Used once all triples are sent.
     * Notify any rules with non-empty buffer to stop waiting for new ones and
     * infere on them
     * 
     * @return the number of rules with non-empty buffers
     */
    public long finishThem() {
        long total = 0;
        for (Rule rule : this.rules) {
            if ((rule.getTripleBuffer().mainBufferOccupation() + rule.getTripleBuffer().secondaryBufferOccupation()) > 0) {
                total++;
            }
            if (logger.isTraceEnabled()) {
                logger.trace("FinishThem " + rule.name() + " buffer : " + rule.getTripleBuffer().mainBufferOccupation() + "," + rule.getTripleBuffer().secondaryBufferOccupation());
            }
            rule.bufferFull();
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
    private boolean match(long[] in, long[] out) {
        if (in.length == 0 || out.length == 0) {
            return true;
        }
        // Broken loops
        for (long l1 : out) {
            for (long l2 : in) {
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
    private long[] extractMatchers(long[] out, long[] in) {
        if (in.length == 0 || out.length == 0) {
            return new long[] {};
        }

        List<Long> matchers = new ArrayList<Long>();
        for (long l1 : out) {
            for (long l2 : in) {
                if (l1 == l2) {
                    matchers.add(l1);
                }
            }
        }

        long[] ms = new long[matchers.size()];

        for (int i = 0; i < matchers.size(); i++) {
            ms[i] = matchers.get(i);
        }

        return ms;
    }

    /**
     * Check every X ms for every TripleBuffer's last flush, and send full
     * buffer signal if needed
     */
    private void timeoutChecker() {
        Runnable checker = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10);
                    for (Rule rule : rules) {
                        if ((rule.getTripleBuffer().getLastFlush() - System.nanoTime()) > (TIMEOUT * 1000000)) {
                            rule.getTripleBuffer().sendFullBuffer();
                        }
                    }
                } catch (InterruptedException e) {
                    logger.error("", e);
                }
            }
        };
        Executors.newSingleThreadExecutor().submit(checker);
    }

}
