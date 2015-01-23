package fr.ujm.tse.lt2c.satin.triplestore.smart.test;

/*
 * #%L
 * SLIDeR
 * %%
 * Copyright (C) 2014 Université Jean Monnet, Saint Etienne
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import fr.ujm.tse.lt2c.satin.slider.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.slider.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.slider.triplestore.ImmutableTriple;
import fr.ujm.tse.lt2c.satin.slider.triplestore.VerticalPartioningTripleStoreRWSmartLock;

public class TestMultiThreadedVerticalPartitioningTripleStoreRWLock {

    public static final int THREADS = 50;

    @Test
    public void test() {

        System.out.println("getall gene TS");
        for (int i = 0; i < 10000; i++) {

            final TripleStore ts = new VerticalPartioningTripleStoreRWSmartLock();
            final Set<Triple> generated = Collections.synchronizedSet(new HashSet<Triple>());

            final ExecutorService executor = Executors.newCachedThreadPool();
            for (int j = 0; j < THREADS; j++) {
                executor.submit(new RunnableAdder(generated, ts));
                // executor.submit(new RunnableGetter(ts));
            }
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.DAYS);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            final Collection<Triple> getall = ts.getAll();
            final int gene = generated.size();

            if (gene != getall.size() || gene != ts.size() || getall.size() != ts.size()) {
                System.out.println(getall.size() + " " + gene + " " + ts.size());
            }

            // if (ts.size() != getall.size()) {
            // System.out.println("Fucked");
            // }
            //
            // if (getall.size() > gene) {
            // getall.removeAll(generated);
            // System.out.println("getall>gene");
            // for (final Triple triple : getall) {
            // System.out.println(triple);
            // }
            // } else if (getall.size() < gene) {
            // generated.removeAll(getall);
            // System.out.println("gene>getall");
            // for (final Triple triple : generated) {
            // System.out.println(triple);
            // }
            // }

            // assertEquals(ts.getAll().size(), ts.size());
            // assertEquals(ts.size(), generated.size());

        }

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
                this.generated.add(t);
                this.ts.add(t);
            }

        }
    };

    class RunnableGetter implements Runnable {
        TripleStore ts;

        public RunnableGetter(final TripleStore ts) {
            super();
            this.ts = ts;
        }

        @Override
        public void run() {
            final Random random = new Random();
            for (int i = 0; i < 10000; i++) {
                if (i % 10 == 0) {
                    this.ts.getbyPredicate(random.nextInt(100));
                }
            }
        }

    }

}
