package fr.ujm.tse.lt2c.satin.rules;

import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.reasoner.ReasonnerStreamed;
import fr.ujm.tse.lt2c.satin.rules.run.AbstractRun;

public class Rule implements BufferListener {

    private static Logger logger = Logger.getLogger(Rule.class);

    /**
     * The Buffer receives the triples, notify the object when it's full
     * This object launch a new Run with the received triples
     * The distributor sends the new triples to the subscribers
     */

    TripleBuffer tripleBuffer;
    AbstractRun ruleRun;

    ExecutorService executor;

    public Rule(AbstractRun ruleRun, ExecutorService executor) {
        super();
        this.tripleBuffer = ruleRun.getTripleBuffer();
        this.ruleRun = ruleRun;
        this.executor = executor;

        this.tripleBuffer.addBufferListener(this);
        this.tripleBuffer.setDebugName(ruleRun.getRuleName());

    }

    @Override
    public boolean bufferFull() {
        if ((ruleRun.getPhaser().getUnarrivedParties() < ReasonnerStreamed.MAX_THREADS) && (this.tripleBuffer.secondaryBufferOccupation() + this.tripleBuffer.mainBufferOccupation()) > 0) {
            this.executor.submit(this.ruleRun);
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
        return this.ruleRun.getInputMatchers();
    }

    public long[] getOutputMatchers() {
        return this.ruleRun.getOutputMatchers();
    }

    public TripleBuffer getTripleBuffer() {
        return this.tripleBuffer;
    }

    public TripleDistributor getTripleDistributor() {
        return this.ruleRun.getDistributor();
    }

    public String name() {
        return this.ruleRun.getRuleName();
    }

    public AbstractRun getRun() {
        return this.ruleRun;
    }
}
