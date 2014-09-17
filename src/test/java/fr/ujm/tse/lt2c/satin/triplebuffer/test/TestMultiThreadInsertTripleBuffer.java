package fr.ujm.tse.lt2c.satin.triplebuffer.test;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;

public class TestMultiThreadInsertTripleBuffer {

    public static final int PROBA = 100000;
    public static final int THREADS = 5;
    public static final int[] BUFFER_SIZES = { 1, 10, 100, 1000 };
    public static final int MAX_VALUE = 1020;
    public static final int TESTS = 10;

    @Test
    public void test() {
        // for (final int bufferSize : BUFFER_SIZES) {
        // for (int k = 0; k < TESTS; k++) {
        //
        // final TripleBuffer tb = new QueuedTripleBufferLock(bufferSize);
        // final SimpleBufferListener listener = new SimpleBufferListener(tb);
        // tb.addBufferListener(listener);
        // final Set<Triple> generated = Collections.synchronizedSet(new HashSet<Triple>());
        //
        // // Test buffer flush
        // final ExecutorService executor = Executors.newCachedThreadPool();
        // for (int j = 0; j < THREADS; j++) {
        // executor.submit(new RunnableAdder(generated, tb, MAX_VALUE));
        // }
        // executor.shutdown();
        // try {
        // executor.awaitTermination(1, TimeUnit.DAYS);
        // } catch (final InterruptedException e) {
        // e.printStackTrace();
        // }
        // // Assert set equality
        // final long listenerf = listener.finish();
        // assertEquals(generated.size(), listenerf);
        //
        // }
        //
        // }
    }

    class RunnableAdder implements Runnable {

        final Set<Triple> generated;
        final TripleBuffer tb;
        final int max;

        public RunnableAdder(final Set<Triple> generated, final TripleBuffer tb, final int max) {
            super();
            this.generated = generated;
            this.tb = tb;
            this.max = max;
        }

        @Override
        public void run() {
            final Random random = new Random();
            while (this.generated.size() <= this.max) {
                final Triple newTriple = new ImmutableTriple(random.nextInt(PROBA), random.nextInt(PROBA), random.nextInt(PROBA));
                this.tb.add(newTriple);
                this.generated.add(newTriple);
            }
        }
    }

    class SimpleBufferListener implements BufferListener {

        TripleBuffer tb;
        AtomicLong counter;

        public SimpleBufferListener(final TripleBuffer tb) {
            super();
            this.tb = tb;
            this.counter = new AtomicLong();
        }

        @Override
        public boolean bufferFull() {
            final long nb = this.tb.clear().size();
            this.counter.addAndGet(nb);
            return true;
        }

        public long finish() {
            final long nb = this.tb.clear().size();
            return this.counter.addAndGet(nb);
        }

    }

}
