package fr.ujm.tse.lt2c.satin.triplebuffer.test;
import java.util.ArrayList;
import java.util.Random;

import fr.ujm.tse.lt2c.satin.buffer.TripleBufferLock;
import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;

public class TestBuffer {

    public static void main(String[] args) {

        TripleBuffer tripleBuffer = new TripleBufferLock();
        ArrayList<Triple> triples = new ArrayList<>();

        BF bf = (new TestBuffer()).new BF(tripleBuffer);

        tripleBuffer.addBufferListener(bf);

        for (int i = 0; i < 1000; i++) {
            Random rnd = new Random();
            Triple t = new ImmutableTriple(rnd.nextLong(), rnd.nextLong(), rnd.nextLong());
            while (!tripleBuffer.add(t)) {
                ;
            }
            triples.add(t);
        }
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long sum = 0;
        for (Triple triple : triples) {
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

        public BF(TripleBuffer tb) {
            this.tb = tb;
        }

        @Override
        public boolean bufferFull() {
            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    count();

                }
            });
            t.start();
            return true;
        }

        private void count() {
            TripleStore triples = this.tb.clear();

            long sum = 0;
            for (Triple triple : triples.getAll()) {
                sum += triple.getSubject();
                sum += triple.getPredicate();
                sum += triple.getObject();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            total_sum += sum;
            System.out.println("SUM: " + sum);
        }

    }

}
