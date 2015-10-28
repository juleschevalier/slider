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
import fr.ujm.tse.lt2c.satin.slider.rules.run.Rule;
import fr.ujm.tse.lt2c.satin.slider.rules.run.RunFactory;

/**
 * @author Jules Chevalier
 *
 */
public class RuleModule implements BufferListener {

    /**
     * The Buffer receives the triples, notify the object when it's full
     * This object launch a new Run with the received triples
     * The distributor sends the new triples to the subscribers
     */

    private static final Logger LOGGER = Logger.getLogger(BufferListener.class);
    private static final int ALPHA = 5;

    private final TripleBuffer tripleBuffer;
    private final TripleDistributor tripleDistributor;
    private final AtomicInteger phaser;
    private final Dictionary dictionary;
    private final TripleStore tripleStore;
    private final Rule run;
    private final int maxThreads;
    private final BufferTimer timer;

    private final long timeout;
    private int level;

    private final ExecutorService executor;

    public RuleModule(final Rule run, final ExecutorService executor, final AtomicInteger phaser, final Dictionary dictionary, final TripleStore tripleStore, final int bufferSize,
            final int maxThreads, final long timeout) {
        super();
        this.run = run;
        this.executor = executor;
        this.phaser = phaser;
        this.dictionary = dictionary;
        this.tripleStore = tripleStore;
        this.maxThreads = maxThreads;
        this.level = 0;
        this.timer = new BufferTimer(timeout, this);
        this.timeout = timeout;

        this.tripleBuffer = new QueuedTripleBufferLock(bufferSize, this.timer, this);
        this.tripleBuffer.setDebugName(RunFactory.getRuleName(run));
        this.tripleBuffer.addBufferListener(this);

        this.tripleDistributor = new TripleDistributor();

    }

    @Override
    public boolean bufferFull() {
        synchronized (this.phaser) {
            if ((this.maxThreads == 0 || this.phaser.get() < this.maxThreads) && !this.tripleBuffer.isEmpty()) {
                this.phaser.incrementAndGet();
                synchronized (this.dictionary) {
                    this.dictionary.notify();
                }
                this.executor.submit(RunFactory.getRunThread(this.run, this.dictionary, this.tripleStore, this.tripleBuffer, this.tripleDistributor, this.phaser));
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean bufferFullTimer(final long triplesToRead) {
        synchronized (this.phaser) {
            if ((this.maxThreads == 0 || this.phaser.get() < this.maxThreads) && !this.tripleBuffer.isEmpty()) {
                this.phaser.incrementAndGet();
                synchronized (this.dictionary) {
                    this.dictionary.notify();
                }
                this.executor.submit(RunFactory.getRunThread(this.run, this.dictionary, this.tripleStore, this.tripleBuffer, this.tripleDistributor, this.phaser, this.timer,
                        triplesToRead));
                return true;
            }
        }
        this.timer.deactivateRule(this.name());
        return false;

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

    public Dictionary getDictionary() {
        return this.dictionary;
    }

    public void setLevel(final int level) {
        this.level = level;
        this.timer.setTimeout(this.leveler(level, this.timeout));
        this.tripleBuffer.setBufferLimit(this.leveler(level, this.tripleBuffer.getBufferLimit()));
    }

    public int getLevel() {
        return this.level;
    }

    private long leveler(final int level, final long parameter) {
        if (level == 0 || level == 1) {
            return parameter;
        }
        return (long) (ALPHA * parameter * Math.log(level) + parameter);
    }

    public BufferTimer getTimer() {
        return this.timer;
    }

}
