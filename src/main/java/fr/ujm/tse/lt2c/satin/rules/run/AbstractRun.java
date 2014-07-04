package fr.ujm.tse.lt2c.satin.rules.run;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.buffer.BufferTimer;
import fr.ujm.tse.lt2c.satin.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.RuleRun;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.utils.GlobalValues;

public abstract class AbstractRun implements RuleRun {

    private static Logger logger = Logger.getLogger(AbstractRun.class);

    protected final Dictionary dictionary;
    protected final TripleStore tripleStore;
    protected final TripleDistributor distributor;
    protected final TripleBuffer tripleBuffer;
    protected String ruleName = "";
    protected final AtomicInteger phaser;
    protected byte complexity = 2;
    protected final BufferTimer timer;

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
        this.phaser = phaser;
        this.timer = this.timer;
    }

    @Override
    public void run() {

        /*
         * Buffer verification
         */

        if (this.tripleBuffer.getOccupation() == 0) {
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
            final TripleStore usableTriples = this.tripleBuffer.clear();

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

            if (!usableTriples.isEmpty()) {
                /* For rules with 2 components */
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

}
