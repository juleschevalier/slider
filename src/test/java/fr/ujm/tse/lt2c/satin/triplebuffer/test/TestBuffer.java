package fr.ujm.tse.lt2c.satin.triplebuffer.test;

import java.util.ArrayList;
import java.util.Random;

import fr.ujm.tse.lt2c.satin.buffer.QueuedTripleBufferLock;
import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;

public class TestBuffer {

    public static void main(final String[] args) {

        final TripleBuffer tripleBuffer = new QueuedTripleBufferLock(100);
        final ArrayList<Triple> triples = new ArrayList<>();

        final BF bf = (new TestBuffer()).new BF(tripleBuffer);

        tripleBuffer.addBufferListener(bf);

        for (int i = 0; i < 1000; i++) {
            final Random rnd = new Random();
            final Triple t = new ImmutableTriple(rnd.nextLong(), rnd.nextLong(), rnd.nextLong());
            while (!tripleBuffer.add(t)) {
                ;
            }
            triples.add(t);
        }
        try {
            Thread.sleep(30000);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        long sum = 0;
        for (final Triple triple : triples) {
            sum += triple.getSubject();
            sum += triple.getPredicate();
            sum += triple.getObject();
        }
        System.out.println("Verif :");
        System.out.println("Total1 = " + sum);
        System.out.println("Total2 = " + bf.total_sum);

    }

    public class BF implements BufferListener {

        TripleBuffer tb;
        public long total_sum = 0;

        public BF(final TripleBuffer tb) {
            this.tb = tb;
        }

        @Override
        public boolean bufferFull() {
            final Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    BF.this.count();

                }
            });
            t.start();
            return true;
        }

        private void count() {
            final TripleStore triples = this.tb.clear();

            long sum = 0;
            for (final Triple triple : triples.getAll()) {
                sum += triple.getSubject();
                sum += triple.getPredicate();
                sum += triple.getObject();
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.total_sum += sum;
            System.out.println("SUM: " + sum);
        }

    }

}
