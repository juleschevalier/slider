package fr.ujm.tse.lt2c.satin.triplebuffer.test;

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

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import fr.ujm.tse.lt2c.satin.slider.buffer.BufferTimer;
import fr.ujm.tse.lt2c.satin.slider.buffer.QueuedTripleBufferLock;
import fr.ujm.tse.lt2c.satin.slider.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.slider.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.slider.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.slider.rules.RuleModule;
import fr.ujm.tse.lt2c.satin.slider.rules.run.Rule;
import fr.ujm.tse.lt2c.satin.slider.triplestore.ImmutableTriple;

public class TestMultiThreadInsertTripleBuffer {

    public static final int PROBA = 100000;
    public static final int THREADS = 5;
    public static final int[] BUFFER_SIZES = { 1, 10, 100, 1000 };
    public static final int MAX_VALUE = 1020;
    public static final int TESTS = 1000;

    @Test
    public void test() {
        for (final int bufferSize : BUFFER_SIZES) {
            for (int k = 0; k < TESTS; k++) {

                final RuleModule ruleModule = new RuleModule(Rule.CAX_SCO, null, new AtomicInteger(), null, null, bufferSize, 0, 0);
                final TripleBuffer tb = new QueuedTripleBufferLock(bufferSize, new BufferTimer(500, ruleModule), ruleModule);
                final SimpleBufferListener listener = new SimpleBufferListener(tb);
                tb.addBufferListener(listener);
                final Set<Triple> generated = Collections.synchronizedSet(new HashSet<Triple>());

                // Test buffer flush
                final ExecutorService executor = Executors.newCachedThreadPool();
                for (int j = 0; j < THREADS; j++) {
                    executor.submit(new RunnableAdder(generated, tb, MAX_VALUE));
                }
                executor.shutdown();
                try {
                    executor.awaitTermination(1, TimeUnit.DAYS);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                // Assert set equality
                final long listenerf = listener.finish();
                assertEquals(generated.size(), listenerf);

            }

        }
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

        @Override
        public boolean bufferFullTimer(final long triplesToRead) {
            // TODO Auto-generated method stub
            return false;
        }

    }

}
