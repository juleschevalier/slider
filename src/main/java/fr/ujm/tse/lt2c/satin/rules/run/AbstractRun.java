package fr.ujm.tse.lt2c.satin.rules.run;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.RuleRun;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.utils.GlobalValues;

public abstract class AbstractRun implements RuleRun {

    private static Logger logger = Logger.getLogger(AbstractRun.class);

    protected Dictionary dictionary;
    protected TripleStore tripleStore;
    protected TripleDistributor distributor;
    protected TripleBuffer tripleBuffer;
    protected String ruleName = "";
    protected int debugThreads;
    protected AtomicInteger phaser;

    /**
     * Constructor
     * 
     * @param dictionary
     * @param tripleStore
     * @param ruleName
     * @param doneSignal
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
        this.debugThreads = 0;
        this.phaser = phaser;

        this.ruleName = this.toString();
        this.distributor.setName(this.ruleName + " Distributor");
    }

    @Override
    public void run() {

        final String runId = "(" + ((Thread.currentThread().hashCode() % 100) + 1000) + ")";

        if (logger.isTraceEnabled()) {
            logger.trace(this.ruleName + runId + " START");
        }

        /*
         * Buffer verification
         */

        if ((this.tripleBuffer.getOccupation()) == 0) {
            if (logger.isTraceEnabled()) {
                logger.trace(this.ruleName + runId + " started for nothing");
                logger.trace(this.ruleName + runId + " END");
            }
            this.phaser.decrementAndGet();
            return;
        }

        try {

            /*
             * Get triples from buffer
             */
            final TripleStore usableTriples = this.tripleBuffer.clear();

            if (usableTriples == null) {
                logger.error(this.ruleName + runId + " NULL usableTriples");
                return;
            }

            this.debugThreads++;
            GlobalValues.incRunsByRule(this.ruleName);

            long debugLoops = 0;

            /*
             * Initialize structure and get new triples from process()
             */
            final Collection<Triple> outputTriples = new HashSet<>();

            if (usableTriples.isEmpty()) {
                logger.warn(this.ruleName + runId + " run without triples");
            } else {
                debugLoops += this.process(usableTriples, this.tripleStore, outputTriples);
                debugLoops += this.process(this.tripleStore, usableTriples, outputTriples);
            }

            /*
             * Add new triples to the TripleStore
             */
            this.addNewTriples(outputTriples);

            if (logger.isDebugEnabled()) {
                logger.debug(this.ruleName + runId + " : " + outputTriples.size() + " triples generated (" + debugLoops + " loops)");
            }

        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            /*
             * Unregister from phaser (notifies the Reasoner the inference is
             * over)
             */
            if (logger.isTraceEnabled()) {
                logger.trace(this.ruleName + runId + " decrement phaser " + this.phaser);
            }
            synchronized (this.phaser) {
                this.phaser.decrementAndGet();
                this.phaser.notifyAll();
            }

            if (logger.isTraceEnabled()) {
                logger.trace(this.ruleName + runId + " END");
            }
        }
    }

    protected abstract int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples);

    protected int addNewTriples(final Collection<Triple> outputTriples) {

        int duplicates = 0;

        if (outputTriples.isEmpty()) {
            return duplicates;
        }

        final Collection<Triple> newTriples = this.tripleStore.addAll(outputTriples);

        if (logger.isTraceEnabled()) {
            logger.trace(this.ruleName + " distribute " + newTriples.size() + " new triples");
        }

        /* End choice */
        this.distributor.distributeAll(newTriples);

        duplicates = outputTriples.size() - newTriples.size();
        GlobalValues.incInferedByRule(this.ruleName, newTriples.size());
        GlobalValues.incDuplicatesByRule(this.ruleName, duplicates);
        return duplicates;
    }

    protected void logDebug(final String message) {
        if (this.getLogger().isDebugEnabled()) {
            final String runId = "(" + ((Thread.currentThread().hashCode() % 100) + 1000) + ")";
            this.getLogger().debug(this.ruleName + runId + " " + message);
        }
    }

    protected void logTrace(final String message) {
        if (this.getLogger().isTraceEnabled()) {
            final String runId = "(" + ((Thread.currentThread().hashCode() % 100) + 1000) + ")";
            this.getLogger().trace(this.ruleName + runId + " " + message);
        }
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

    public int getThreads() {
        return this.debugThreads;
    }

    @Override
    public String toString() {
        return this.ruleName;
    }

    public AtomicInteger getPhaser() {
        return this.phaser;
    }

}
