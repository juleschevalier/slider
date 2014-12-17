package fr.ujm.tse.lt2c.satin.slider.rules;

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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.slider.buffer.BufferTimer;
import fr.ujm.tse.lt2c.satin.slider.buffer.QueuedTripleBufferLock;
import fr.ujm.tse.lt2c.satin.slider.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.slider.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.slider.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.slider.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.slider.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.slider.rules.run.AvaibleRuns;
import fr.ujm.tse.lt2c.satin.slider.rules.run.RunFactory;

/**
 * @author Jules Chevalier
 *
 */
public class Rule implements BufferListener {

    /**
     * The Buffer receives the triples, notify the object when it's full
     * This object launch a new Run with the received triples
     * The distributor sends the new triples to the subscribers
     */

    private final Logger LOGGER = Logger.getLogger(BufferListener.class);

    private final TripleBuffer tripleBuffer;
    private final TripleDistributor tripleDistributor;
    private final AtomicInteger phaser;
    private final Dictionary dictionary;
    private final TripleStore tripleStore;
    private final AvaibleRuns run;
    private final int maxThreads;
    private final BufferTimer timer;

    private final ExecutorService executor;

    public Rule(final AvaibleRuns run, final ExecutorService executor, final AtomicInteger phaser, final Dictionary dictionary, final TripleStore tripleStore,
            final int bufferSize, final int maxThreads, final BufferTimer timer) {
        super();
        this.run = run;
        this.executor = executor;
        this.phaser = phaser;
        this.dictionary = dictionary;
        this.tripleStore = tripleStore;
        this.maxThreads = maxThreads;
        this.timer = timer;

        this.tripleBuffer = new QueuedTripleBufferLock(bufferSize, this.timer, this);
        this.tripleBuffer.setDebugName(RunFactory.getRuleName(run));
        this.tripleBuffer.addBufferListener(this);

        this.tripleDistributor = new TripleDistributor();

    }

    @Override
    public boolean bufferFull() {
        synchronized (this.phaser) {
            if (this.tripleBuffer.size() > 0) {
                this.phaser.incrementAndGet();
                synchronized (this.dictionary) {
                    this.dictionary.notify();
                }
                this.executor.submit(RunFactory.getRunThread(this.run, this.dictionary, this.tripleStore, this.tripleBuffer, this.tripleDistributor,
                        this.phaser));
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean bufferFullTimer(final long triplesToRead) {
        synchronized (this.phaser) {
            if ((this.maxThreads == 0 || this.phaser.get() < this.maxThreads) && this.tripleBuffer.size() > 0) {
                this.phaser.incrementAndGet();
                synchronized (this.dictionary) {
                    this.dictionary.notify();
                }
                this.executor.submit(RunFactory.getRunThread(this.run, this.dictionary, this.tripleStore, this.tripleBuffer, this.tripleDistributor,
                        this.phaser, this.timer, triplesToRead));
                return true;
            }
            return false;
        }

    }

    public long[] getInputMatchers() {
        return RunFactory.getInputMatchers(this.run);
    }

    public long[] getOutputMatchers() {
        return RunFactory.getOutputMatchers(this.run);
    }

    public TripleBuffer getTripleBuffer() {
        return this.tripleBuffer;
    }

    public TripleDistributor getTripleDistributor() {
        return this.tripleDistributor;
    }

    public String name() {
        return RunFactory.getRuleName(this.run);
    }

}
