package fr.ujm.tse.lt2c.satin.tools;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import fr.ujm.tse.lt2c.satin.buffer.TripleBufferLock;
import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

public class TripleBufferTester implements BufferListener {

        TripleBuffer tripleBuffer;
        AtomicLong sum;
        static Random random;
        ExecutorService executor;
        static Set<Triple> totalTriples;
        static {
                totalTriples = Collections.synchronizedSet(new HashSet<Triple>());
        }

        public TripleBufferTester() {
                this.tripleBuffer = new TripleBufferLock();
                this.tripleBuffer.addBufferListener(this);
                this.executor = Executors.newCachedThreadPool();
                sum = new AtomicLong();
                random = new Random();
        }

        public Collection<Triple> getTriples() {
                return tripleBuffer.getCollection();
        }

        public void add(Triple triple) {
                this.tripleBuffer.add(triple);
        }

        @Override
        public void bufferFull() {
                Thread t = new Thread(new Runnable() {
                        public void run() {
                                TripleStore ts = tripleBuffer.clear();
                                totalTriples.addAll(ts.getAll());
                                try {
                                        Thread.sleep(random.nextInt(5));
                                } catch (InterruptedException e) {
                                        e.printStackTrace();
                                }
                                computeSum(ts);
                        }
                });
                this.executor.submit(t);

        }

        public void close() {

                Thread t = new Thread(new Runnable() {
                        public void run() {
                                TripleStore ts = tripleBuffer.flush();
                                // try {
                                // Thread.sleep(random.nextInt(8));
                                // } catch (InterruptedException e) {
                                // e.printStackTrace();
                                // }
                                computeSum(ts);
                        }
                });
                this.executor.submit(t);
        }

        private void computeSum(TripleStore triples) {
                if (triples == null) {
                        System.out.println("NULL Collection");
                        return;
                }
                long tempSum = 0;
                for (Triple triple : triples.getAll()) {
                        tempSum++;// = triple.getObject() + triple.getSubject() +
                                                // triple.getPredicate();
                        // try {
                        // Thread.sleep(random.nextInt(20));
                        // } catch (InterruptedException e) {
                        // e.printStackTrace();
                        // }
                }
                // System.out.println("Partial sum (" + triples.size() + " elements) : "
                // + tempSum);
                sum.addAndGet(tempSum);
        }

        public long getSum() {
                return totalTriples.size();
        }

        public static void main(String[] args) {

                for (int i = 0; i < 50; i++) {
                        test();
                }

        }

        private static void test() {
                TripleBufferTester.totalTriples = Collections.synchronizedSet(new HashSet<Triple>());
                TripleBufferTester tbt = new TripleBufferTester();
                TripleStore ts = new VerticalPartioningTripleStoreRWLock();

                for (int i = 0; i < 10000; i++) {
                        Triple t = new ImmutableTriple(random.nextInt(10),
                                        random.nextInt(100), random.nextInt(10));
                        ts.add(t);
                        tbt.add(t);
                }
                tbt.close();
                tbt.executor.shutdown();

                long sum = 0;
                sum = ts.size();
                // System.out.println("Real sum : " + sum);
                try {
                        tbt.executor.awaitTermination(1, TimeUnit.DAYS);
                        // Thread.sleep(10000);
                } catch (InterruptedException e) {
                        e.printStackTrace();
                }
                // System.out.println("Calculated sum : " + tbt.getSum());
                if (sum == tbt.getSum())
                        System.out.println("Everything seems to be ok");
                else {
                        System.out.println("The sums are not egals (" + sum + " and "
                                        + tbt.getSum() + ")");
                        // Set gros = new HashSet<>(triples);
                        // gros.removeAll(tbt.getTriples());
                        // System.out.println(triples.size());
                
                
                        // System.out.println(gros.size());
                }
        }

}