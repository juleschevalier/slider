package fr.ujm.tse.lt2c.satin.triplestore.test;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

public class TestMultiThreadAddVerticalPartitioningTripleStoreRWLock {

    public static final int THREADS = 50;

    @Test
    public void test() {
        final TripleStore ts = new VerticalPartioningTripleStoreRWLock();
        final Set<Triple> generated = Collections.synchronizedSet(new HashSet<Triple>());

        final ExecutorService executor = Executors.newCachedThreadPool();
        for (int j = 0; j < THREADS; j++) {
            executor.submit(new RunnableAdder(generated, ts));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
            // Thread.sleep(10000);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(ts.size(), generated.size());
        assertEquals(ts.getAll().size(), generated.size());

    }

    class RunnableAdder implements Runnable {
        Set<Triple> generated;
        TripleStore ts;

        public RunnableAdder(final Set<Triple> generated, final TripleStore ts) {
            super();
            this.generated = generated;
            this.ts = ts;
        }

        @Override
        public void run() {
            final Random random = new Random();
            for (int i = 0; i < 10000; i++) {
                final Triple t = new ImmutableTriple(random.nextInt(100), random.nextInt(100), random.nextInt(100));
                this.ts.add(t);
                this.generated.add(t);
            }

        }

    };

}
