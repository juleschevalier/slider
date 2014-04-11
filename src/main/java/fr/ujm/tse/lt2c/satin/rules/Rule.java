package fr.ujm.tse.lt2c.satin.rules;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import fr.ujm.tse.lt2c.satin.buffer.QueuedTripleBufferLock;
import fr.ujm.tse.lt2c.satin.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.rules.run.AvaibleRuns;
import fr.ujm.tse.lt2c.satin.rules.run.RunFactory;

public class Rule implements BufferListener {

    /**
     * The Buffer receives the triples, notify the object when it's full
     * This object launch a new Run with the received triples
     * The distributor sends the new triples to the subscribers
     */

    private final QueuedTripleBufferLock tripleBuffer;
    private final TripleDistributor tripleDistributor;
    private final AtomicInteger phaser;
    private final Dictionary dictionary;
    private final TripleStore tripleStore;
    private final AvaibleRuns run;
    private final int maxThreads;

    ExecutorService executor;

    public Rule(final AvaibleRuns run, final ExecutorService executor, final AtomicInteger phaser, final Dictionary dictionary, final TripleStore tripleStore,
            final int bufferSize, final int maxThreads) {
        super();
        this.run = run;
        this.executor = executor;
        this.phaser = phaser;
        this.dictionary = dictionary;
        this.tripleStore = tripleStore;
        this.maxThreads = maxThreads;

        this.tripleBuffer = new QueuedTripleBufferLock(bufferSize);
        this.tripleBuffer.setDebugName(RunFactory.getRuleName(run));
        this.tripleBuffer.addBufferListener(this);

        this.tripleDistributor = new TripleDistributor();

    }

    @Override
    public boolean bufferFull() {
        if ((this.phaser.get() < this.maxThreads) && ((this.tripleBuffer.getOccupation()) > 0)) {
            this.phaser.incrementAndGet();
            this.executor
                    .submit(RunFactory.getRunInstance(this.run, this.dictionary, this.tripleStore, this.tripleBuffer, this.tripleDistributor, this.phaser));
            return true;
        }
        return false;
    }

    public long[] getInputMatchers() {
        return RunFactory.getInputMatchers(this.run);
    }

    public long[] getOutputMatchers() {
        return RunFactory.getOutputMatchers(this.run);
    }

    public TripleBuffer getTripleBuffer() {
        return this.tripleBuffer;
    }

    public TripleDistributor getTripleDistributor() {
        return this.tripleDistributor;
    }

    public String name() {
        return RunFactory.getRuleName(this.run);
    }
}
