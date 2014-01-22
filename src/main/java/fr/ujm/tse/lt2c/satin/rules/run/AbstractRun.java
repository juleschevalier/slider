package fr.ujm.tse.lt2c.satin.rules.run;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Phaser;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.buffer.TripleBufferLock;
import fr.ujm.tse.lt2c.satin.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.RuleRun;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;

public abstract class AbstractRun implements RuleRun {

    private static Logger logger = Logger.getLogger(AbstractRun.class);

    protected Dictionary dictionary;
    protected TripleStore tripleStore;
    protected TripleDistributor distributor;
    protected TripleBuffer tripleBuffer;
    protected String ruleName = "";
    protected int debugThreads;
    protected Phaser phaser;

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
    public AbstractRun(Dictionary dictionary, TripleStore tripleStore, Phaser pĥaser, String ruleName) {
        this.dictionary = dictionary;
        this.tripleStore = tripleStore;
        this.ruleName = ruleName;
        this.distributor = new TripleDistributor();
        this.tripleBuffer = new TripleBufferLock();
        this.debugThreads = 0;
        this.phaser = pĥaser;

        this.distributor.setName(ruleName + " Distributor");
    }

    @Override
    public void run() {

        final String runId = "(" + (Thread.currentThread().hashCode() % 100 + 1000) + ")";

        if (logger.isTraceEnabled()) {
            logger.trace(this.ruleName + runId + " START");
        }

        /*
         * Buffer verification
         */

        if (this.tripleBuffer.mainBufferOccupation() + this.tripleBuffer.secondaryBufferOccupation() == 0) {
            if (logger.isTraceEnabled()) {
                logger.trace(this.ruleName + runId + " started for nothing");
                logger.trace(this.ruleName + runId + " END");
            }
            return;
        }

        try {

            /*
             * Get triples from buffer
             */
            TripleStore usableTriples = this.tripleBuffer.clear();

            if (usableTriples == null) {
                logger.error(this.ruleName + runId + " NULL usableTriples");
                return;
            }

            /*
             * Register on phaser
             * Notifies the Reasoner this thread infers
             */
            if (logger.isTraceEnabled()) {
                logger.trace(this.ruleName + runId + " register on " + this.phaser);
            }
            if (this.phaser.register() < 0) {
                logger.warn(ruleName + runId + " register on closed phaser");
            }
            this.debugThreads++;

            long debugLoops = 0;

            /*
             * Initialize structure and get new triples from process()
             */
            Collection<Triple> outputTriples = new HashSet<>();

            if (usableTriples.isEmpty()) {
                logger.warn(this.ruleName + runId + " run without triples");
            } else {
                debugLoops += process(usableTriples, tripleStore, outputTriples);
                debugLoops += process(tripleStore, usableTriples, outputTriples);
            }

            /*
             * Add new triples to the TripleStore
             */
            addNewTriples(outputTriples);

            if (logger.isDebugEnabled()) {
                logger.debug(this.ruleName + runId + " : " + outputTriples.size() + " triples generated (" + debugLoops + " loops)");
            }

        } catch (Exception e) {
            logger.error("", e);
        } finally {
            /*
             * Unregister from phaser (notifies the Reasoner the inference is
             * over)
             */
            if (logger.isTraceEnabled()) {
                logger.trace(this.ruleName + runId + " arriveAndDeregister on " + this.phaser);
            }
            if (this.phaser.arrive()/* AndDeregister() */< 0) {
                logger.warn(ruleName + runId + " deregister on closed phaser");
            }
            if (logger.isTraceEnabled()) {
                logger.trace(this.ruleName + runId + " END");
            }
        }
    }

    protected abstract int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples);

    protected int addNewTriples(Collection<Triple> outputTriples) {
        int duplicates = 0;

        if (outputTriples.isEmpty()) {
            return duplicates;
        }

        ArrayList<Triple> newTriples = new ArrayList<>();
        for (Triple triple : outputTriples) {
            if (!tripleStore.contains(triple)) {
                tripleStore.add(triple);
                newTriples.add(triple);
            } else {
                logTrace(dictionary.printTriple(triple) + " already present");
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace(this.ruleName + " distribute " + newTriples.size() + " new triples");
        }
        distributor.distribute(newTriples);
        return duplicates;
    }

    protected void logDebug(String message) {
        if (getLogger().isDebugEnabled()) {
            final String runId = "(" + (Thread.currentThread().hashCode() % 100 + 1000) + ")";
            getLogger().debug(ruleName + runId + " " + message);
        }
    }

    protected void logTrace(String message) {
        if (getLogger().isTraceEnabled()) {
            final String runId = "(" + (Thread.currentThread().hashCode() % 100 + 1000) + ")";
            getLogger().trace(ruleName + runId + " " + message);
        }
    }

    public String getRuleName() {
        return ruleName;
    }

    public TripleBuffer getTripleBuffer() {
        return this.tripleBuffer;
    }

    public TripleDistributor getDistributor() {
        return this.distributor;
    }

    public abstract long[] getInputMatchers();

    public abstract long[] getOutputMatchers();

    public int getThreads() {
        return debugThreads;
    }

    @Override
    public String toString() {
        return this.ruleName;
    }

    public Phaser getPhaser() {
        return this.phaser;
    }

}
