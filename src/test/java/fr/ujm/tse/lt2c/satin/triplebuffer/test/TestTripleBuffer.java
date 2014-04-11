package fr.ujm.tse.lt2c.satin.triplebuffer.test;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import fr.ujm.tse.lt2c.satin.buffer.QueuedTripleBufferLock;
import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;

public class TestTripleBuffer {

    public static final int PROBA = 100;
    public static final int BUFFER_SIZE = 10;
    public static final int MAX_VALUE = 1000;

    @Test
    public void basicTest() {
        final TripleBuffer tb = new QueuedTripleBufferLock(1);
        tb.add(new ImmutableTriple(0, 0, 0));
        tb.add(new ImmutableTriple(0, 0, 0));
        tb.add(new ImmutableTriple(0, 0, 0));
        tb.add(new ImmutableTriple(0, 0, 0));
        Assert.assertEquals(4, tb.getOccupation());
        Assert.assertEquals(1, tb.clear().size());
        Assert.assertEquals(3, tb.getOccupation());
    }

    @Test
    public void test() {
        TripleBuffer tb = new QueuedTripleBufferLock(BUFFER_SIZE);
        final Set<Triple> generated = new HashSet<>();

        // Test buffer flush
        final Random random = new Random();
        while (generated.size() < (tb.getBufferLimit() - 1)) {
            final Triple t = new ImmutableTriple(random.nextInt(PROBA), random.nextInt(PROBA), random.nextInt(PROBA));
            tb.add(t);
            generated.add(t);
        }
        assertEquals(generated.size(), tb.getOccupation());

        // ----Clear test
        final SimpleBufferListener sbl = new SimpleBufferListener(tb);
        tb.addBufferListener(sbl);
        assertEquals(1, tb.getBufferListeners().size());

        // Switchy must occur here
        tb.add(new ImmutableTriple(random.nextInt(PROBA), random.nextInt(PROBA), random.nextInt(PROBA)));
        assertEquals(0, tb.getOccupation());

        // ---- Overflow test
        tb = new QueuedTripleBufferLock(BUFFER_SIZE);
        final OverFlowListener ofl = new OverFlowListener(tb);
        tb.addBufferListener(ofl);
        assertEquals(1, tb.getBufferListeners().size());
        generated.clear();

        while (generated.size() < ((tb.getBufferLimit() * 3) + 3)) {
            final Triple t = new ImmutableTriple(random.nextInt(PROBA), random.nextInt(PROBA), random.nextInt(PROBA));
            tb.add(t);
            generated.add(t);
        }
        assertEquals(3, tb.getOccupation());

    }

    class SimpleBufferListener implements BufferListener {

        TripleBuffer tb;

        public SimpleBufferListener(final TripleBuffer tb) {
            super();
            this.tb = tb;
        }

        @Override
        public boolean bufferFull() {
            this.tb.clear();
            return true;
        }

    }

    class OverFlowListener implements BufferListener {

        TripleBuffer tb;

        public OverFlowListener(final TripleBuffer tb) {
            super();
            this.tb = tb;
        }

        @Override
        public boolean bufferFull() {
            // Do shit
            try {
                Thread.sleep(10);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            this.tb.clear();
            return true;
        }

    }
}
