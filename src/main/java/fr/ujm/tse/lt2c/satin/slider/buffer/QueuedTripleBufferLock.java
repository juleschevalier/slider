package fr.ujm.tse.lt2c.satin.slider.buffer;

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
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.slider.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.slider.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.slider.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.slider.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.slider.rules.Rule;
import fr.ujm.tse.lt2c.satin.slider.triplestore.VerticalPartioningTripleStoreRWLock;
import fr.ujm.tse.lt2c.satin.slider.utils.MonitoredValues;

/**
 * Concurrent implementation of {@link TripleBuffer} using a ConcurrentLinkedQueue as buffer
 * 
 * @author Jules Chevalier
 * @see TripleBuffer
 */
public class QueuedTripleBufferLock implements TripleBuffer {
    private static Logger logger = Logger.getLogger(QueuedTripleBufferLock.class);

    public static final int DEFAULT_BUFFER_SIZE = 100000;

    /* Limit of the buffer (adding the last triple calls bufferfull()) */
    private final long bufferSize;

    private final Queue<Triple> triples;
    private final ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
    private final Collection<BufferListener> bufferListeners;
    private final AtomicLong currentBuffer;
    private final BufferTimer timer;
    private final Rule rule;
    private final AtomicLong size;

    private String debugName;

    /**
     * Constructor
     * 
     * @param bufferSize
     * @param timer
     * @param rule
     */
    public QueuedTripleBufferLock(final long bufferSize, final BufferTimer timer, final Rule rule) {
        this.triples = new ConcurrentLinkedQueue<>();
        this.bufferListeners = new HashSet<>();
        this.currentBuffer = new AtomicLong();
        this.bufferSize = bufferSize;
        this.timer = timer;
        this.rule = rule;
        this.size = new AtomicLong();
    }

    @Override
    public void add(final Triple triple) {
        try {
            this.rwlock.writeLock().lock();
            this.triples.add(triple);
            this.size.incrementAndGet();
            this.timer.notifyAdd(this.rule);
            if (this.currentBuffer.incrementAndGet() >= this.bufferSize) {
                this.currentBuffer.set(0);
                for (final BufferListener bufferListener : this.bufferListeners) {
                    bufferListener.bufferFull();
                }
            }
            MonitoredValues.updateBuffer(this.rule.name(), this.getOccupation());

        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.rwlock.writeLock().unlock();
        }
    }

    @Override
    public void addAll(final Collection<Triple> triples) {
        try {
            this.rwlock.writeLock().lock();
            this.triples.addAll(triples);
            this.timer.notifyAdd(this.rule);
            this.size.addAndGet(triples.size());
            this.currentBuffer.set(this.size() % (int) this.bufferSize);
            for (int i = 0; i < this.size() / this.bufferSize; i++) {
                for (final BufferListener bufferListener : this.bufferListeners) {
                    bufferListener.bufferFull();
                }
            }
            MonitoredValues.updateBuffer(this.rule.name(), this.getOccupation());

        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.rwlock.writeLock().unlock();
        }
    }

    @Override
    public TripleStore clear(final long triplesToRead) {
        TripleStore ts = null;
        try {
            this.rwlock.writeLock().lock();

            ts = new VerticalPartioningTripleStoreRWLock();

            int i = 0;
            Triple triple = this.triples.poll();
            while (triple != null && i++ < triplesToRead) {
                ts.add(triple);
                if (i < this.bufferSize) {
                    triple = this.triples.poll();
                } else {
                    triple = null;
                }
            }
            this.size.addAndGet(-ts.size());
            MonitoredValues.updateBuffer(this.rule.name(), this.getOccupation());
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.rwlock.writeLock().unlock();
            synchronized (this) {
                this.notifyAll();
            }
        }

        return ts;
    }

    @Override
    public TripleStore clear() {
        TripleStore ts = null;
        try {
            this.rwlock.writeLock().lock();

            ts = new VerticalPartioningTripleStoreRWLock();

            int i = 0;
            Triple triple = this.triples.poll();
            while (triple != null && i++ < this.bufferSize) {
                ts.add(triple);
                if (i < this.bufferSize) {
                    triple = this.triples.poll();
                } else {
                    triple = null;
                }
            }
            this.size.addAndGet(-ts.size());
            MonitoredValues.updateBuffer(this.rule.name(), this.getOccupation());
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.rwlock.writeLock().unlock();
            synchronized (this) {
                this.notifyAll();
            }
        }

        return ts;
    }

    @Override
    public void addBufferListener(final BufferListener bufferListener) {
        this.bufferListeners.add(bufferListener);
    }

    @Override
    public Collection<Triple> getCollection() {
        Collection<Triple> triplesList = null;
        try {
            this.rwlock.readLock().lock();
            triplesList = this.triples;
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.rwlock.readLock().unlock();
        }
        return triplesList;
    }

    @Override
    public long getBufferLimit() {
        return this.bufferSize;
    }

    @Override
    public long getOccupation() {
        return this.currentBuffer.get();
    }

    @Override
    public long size() {
        return this.size.get();
    }

    @Override
    public Collection<BufferListener> getBufferListeners() {
        return this.bufferListeners;
    }

    @Override
    public void sendFullBuffer() {
        try {
            this.rwlock.writeLock().lock();
            if (!(this.currentBuffer.get() > this.bufferSize)) {
                for (final BufferListener bufferListener : this.bufferListeners) {
                    bufferListener.bufferFull();
                }
            }
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.rwlock.writeLock().unlock();
        }

    }

    @Override
    public String getDebugName() {
        return this.debugName;
    }

    @Override
    public void setDebugName(final String debugName) {
        this.debugName = debugName;
    }

    @Override
    public void timerCall(final long triples) {
        // System.out.println(this.currentBuffer.get());
        this.currentBuffer.addAndGet(-triples);
        // System.out.println(this.currentBuffer.get());
    }
}