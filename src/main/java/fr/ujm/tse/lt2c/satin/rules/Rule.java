package fr.ujm.tse.lt2c.satin.rules;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.buffer.TripleBufferLock;
import fr.ujm.tse.lt2c.satin.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.reasoner.ReasonnerStreamed;
import fr.ujm.tse.lt2c.satin.rules.run.AvaibleRuns;
import fr.ujm.tse.lt2c.satin.rules.run.RunFactory;

public class Rule implements BufferListener {

    private static Logger logger = Logger.getLogger(Rule.class);

    /**
     * The Buffer receives the triples, notify the object when it's full
     * This object launch a new Run with the received triples
     * The distributor sends the new triples to the subscribers
     */

    private final TripleBuffer tripleBuffer;
    private final TripleDistributor tripleDistributor;
    private final AtomicInteger phaser;
    private final Dictionary dictionary;
    private final TripleStore tripleStore;
    private final AvaibleRuns run;

    ExecutorService executor;

    public Rule(AvaibleRuns run, ExecutorService executor, AtomicInteger phaser, Dictionary dictionary, TripleStore tripleStore) {
        super();
        this.run = run;
        this.executor = executor;
        this.phaser = phaser;
        this.dictionary = dictionary;
        this.tripleStore = tripleStore;

        this.tripleBuffer = new TripleBufferLock();
        this.tripleBuffer.addBufferListener(this);
        this.tripleBuffer.setDebugName(RunFactory.getRuleName(run));

        this.tripleDistributor = new TripleDistributor();
        Executors.newSingleThreadExecutor().submit(tripleDistributor);

    }

    @Override
    public boolean bufferFull() {
        if ((this.phaser.get() < ReasonnerStreamed.MAX_THREADS) && (this.tripleBuffer.secondaryBufferOccupation() + this.tripleBuffer.mainBufferOccupation()) > 0) {
            this.executor.submit(RunFactory.getRunInstance(run, dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser));
            /*
             * For monothread :
             * this.ruleRun.run();
             */
            return true;
        }
        if (logger.isTraceEnabled()) {
            logger.trace(name() + " Missing space or no more necessary");
        }
        return false;
    }

    public long[] getInputMatchers() {
        return RunFactory.getInputMatchers(run);
    }

    public long[] getOutputMatchers() {
        return RunFactory.getOutputMatchers(run);
    }

    public TripleBuffer getTripleBuffer() {
        return this.tripleBuffer;
    }

    public TripleDistributor getTripleDistributor() {
        return this.tripleDistributor;
    }

    public String name() {
        return RunFactory.getRuleName(run);
    }
}
