package fr.ujm.tse.lt2c.satin.triplebuffer.test;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import fr.ujm.tse.lt2c.satin.buffer.QueuedTripleBufferLock;
import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;

public class TestMultiThreadInsertTripleBuffer {

    public final static int PROBA = 1000000;
    public static final int THREADS = 50;

    @Test
    public void test() {
        for (int k = 0; k < 150; k++) {
            final TripleBuffer tb = new QueuedTripleBufferLock(100);
            final SimpleBufferListener listener = new SimpleBufferListener(tb);
            tb.addBufferListener(listener);
            final Set<Triple> generated = Collections.synchronizedSet(new HashSet<Triple>());

            // Test buffer flush
            final ExecutorService executor = Executors.newCachedThreadPool();
            final int maxvalue = 45000;
            for (int j = 0; j < THREADS; j++) {
                executor.submit(new RunnableAdder(generated, tb, maxvalue));
            }
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.DAYS);
                // Thread.sleep(10000);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            // Assert set equality
            final long listenerf = listener.finish();
            // System.out.println(listenerf);
            assertEquals(generated.size(), listenerf);

        }
    }

    class RunnableAdder implements Runnable {

        Set<Triple> generated;
        TripleBuffer tb;
        int max;

        public RunnableAdder(final Set<Triple> generated, final TripleBuffer tb, final int max) {
            super();
            this.generated = generated;
            this.tb = tb;
            this.max = max;
        }

        @Override
        public void run() {
            final Random random = new Random();
            while (this.generated.size() < this.max) {
                final Triple newTriple = new ImmutableTriple(random.nextInt(PROBA), random.nextInt(PROBA), random.nextInt(PROBA));
                this.generated.add(newTriple);
                while (!this.tb.add(newTriple)) {
                    ;
                }
            }
        }

    }

    class SimpleBufferListener implements BufferListener {

        TripleBuffer tb;
        long counter;

        public SimpleBufferListener(final TripleBuffer tb) {
            super();
            this.tb = tb;
            this.counter = 0;
        }

        @Override
        public boolean bufferFull() {
            // assertEquals(0, this.tb.mainBufferOccupation());
            // assertEquals(this.tb.getBufferLimit(), this.tb.secondaryBufferOccupation());
            // final TripleStore emptyedBuffer = this.tb.clear();
            // assertEquals(emptyedBuffer.size(), this.tb.getBufferLimit());
            // // TODO possible shit here
            // assertEquals(0, this.tb.secondaryBufferOccupation());
            // this.counter += emptyedBuffer.size();
            return true;
        }

        public long finish() {
            return this.counter + this.tb.clear().size();
        }

    }

}
