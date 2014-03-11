package fr.ujm.tse.lt2c.satin.buffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.rules.Rule;

/**
 * This class is in charge of linking all the buffers together
 * 
 * @author Jules Chevalier
 */
public class TripleManager {

    private static final Logger logger = Logger.getLogger(TripleManager.class);

    List<Rule> rules;
    TripleDistributor generalDistributor;

    /**
     * Constructor
     */
    public TripleManager() {
        super();
        this.rules = new ArrayList<>();
        this.generalDistributor = new TripleDistributor();
        this.generalDistributor.setName("T_MANAGER");
    }

    /**
     * Add a rule and connect it with other rules (TripleDistributors with
     * TripleBuffers)
     * 
     * @param newRule
     * @see Rule
     */
    public void addRule(final Rule newRule) {
        this.rules.add(newRule);
        this.generalDistributor.addSubscriber(newRule.getTripleBuffer(), newRule.getInputMatchers());

        for (final Rule rule : this.rules) {
            if (this.match(newRule.getOutputMatchers(), rule.getInputMatchers())) {
                final long[] matchers = this.extractMatchers(newRule.getOutputMatchers(), rule.getInputMatchers());
                newRule.getTripleDistributor().addSubscriber(rule.getTripleBuffer(), matchers);
            }
            if ((rule != newRule) && this.match(rule.getOutputMatchers(), newRule.getInputMatchers())) {
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
     * Used once all triples are sent.
     * Notify any rules with non-empty buffer to stop waiting for new ones and
     * infere on them
     * 
     * @return the number of rules with non-empty buffers
     */
    public long flushBuffers() {
        long total = 0;
        for (final Rule rule : this.rules) {
            if ((rule.getTripleBuffer().getOccupation()) > 0) {
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
        if ((in.length == 0) || (out.length == 0)) {
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
        if ((in.length == 0) || (out.length == 0)) {
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
