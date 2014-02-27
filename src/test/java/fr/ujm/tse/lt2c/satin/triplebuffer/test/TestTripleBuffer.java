package fr.ujm.tse.lt2c.satin.triplebuffer.test;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import fr.ujm.tse.lt2c.satin.buffer.QueuedTripleBufferLock;
import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;

public class TestTripleBuffer {

    public final static int PROBA = 1000000;

    @Test
    public void test() {
        TripleBuffer tb = new QueuedTripleBufferLock(100);
        final Set<Triple> generated = new HashSet<>();

        // Test buffer flush
        final Random random = new Random();
        while (generated.size() < (tb.getBufferLimit() - 1)) {
            final Triple t = new ImmutableTriple(random.nextInt(PROBA), random.nextInt(PROBA), random.nextInt(PROBA));
            while (!tb.add(t)) {
                ;
            }
            generated.add(t);
        }
        assertEquals(generated.size(), tb.clear().size());

        // ----Clear test
        final SimpleBufferListener sbl = new SimpleBufferListener(tb);
        tb.addBufferListener(sbl);
        assertEquals(1, tb.getBufferListeners().size());
        // Switchy must occur here
        tb.add(new ImmutableTriple(random.nextInt(PROBA), random.nextInt(PROBA), random.nextInt(PROBA)));
        // System.out.println(tb.mainBufferOccupation()+" "+tb.secondaryBufferOccupation());
        assertEquals(1, tb.getOccupation());

        // ---- Overflow test
        tb = new QueuedTripleBufferLock(100);
        final OverFlowListener ofl = new OverFlowListener(tb);
        tb.addBufferListener(ofl);
        assertEquals(1, tb.getBufferListeners().size());
        generated.clear();
        while (generated.size() < ((tb.getBufferLimit() * 3) + 3)) {
            final Triple t = new ImmutableTriple(random.nextInt(PROBA), random.nextInt(PROBA), random.nextInt(PROBA));
            boolean ok = false;
            while (!tb.add(t)) {
                System.out.print(".");
                ok = true;
            }
            if (ok) {
                System.out.println();
            }
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
            // System.out.println("BufferFull called");
            // assertEquals(0, this.tb.mainBufferOccupation());
            // assertEquals(this.tb.getBufferLimit(), this.tb.secondaryBufferOccupation());
            // final TripleStore emptyedBuffer = this.tb.clear();
            // assertEquals(emptyedBuffer.size(), this.tb.getBufferLimit());
            // assertEquals(0, this.tb.mainBufferOccupation());
            // assertEquals(0, this.tb.secondaryBufferOccupation());
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
            // System.out.println("BufferFull called");
            // Do shit
            try {
                Thread.sleep(500);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            this.tb.clear();
            return true;
        }

    }
}
