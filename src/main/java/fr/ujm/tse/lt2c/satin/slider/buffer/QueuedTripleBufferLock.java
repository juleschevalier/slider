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
import fr.ujm.tse.lt2c.satin.slider.rules.RuleModule;
import fr.ujm.tse.lt2c.satin.slider.triplestore.VerticalPartioningTripleStore;

/**
 * Concurrent implementation of {@link TripleBuffer} using a ConcurrentLinkedQueue as buffer
 * 
 * @author Jules Chevalier
 * @see TripleBuffer
 */
public class QueuedTripleBufferLock implements TripleBuffer {
    private static final Logger LOGGER = Logger.getLogger(QueuedTripleBufferLock.class);

    public static final int DEFAULT_BUFFER_SIZE = 100000;

    /* Limit of the buffer (adding the last triple calls bufferfull()) */
    private final long bufferSize;

    private final Queue<Triple> triples;
    private final ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
    private final Collection<BufferListener> bufferListeners;
    private final AtomicLong occupation;
    private final BufferTimer timer;
    private final RuleModule ruleModule;
    private final AtomicLong size;

    private String debugName;

    /**
     * Constructor
     * 
     * @param bufferSize
     * @param timer
     * @param rule
     */
    public QueuedTripleBufferLock(final long bufferSize, final BufferTimer timer, final RuleModule ruleModule) {
        this.triples = new ConcurrentLinkedQueue<>();
        this.bufferListeners = new HashSet<>();
        this.occupation = new AtomicLong();
        this.bufferSize = bufferSize;
        this.timer = timer;
        this.ruleModule = ruleModule;
        this.size = new AtomicLong();
    }

    @Override
    public void add(final Triple triple) {
        try {
            this.rwlock.writeLock().lock();
            this.triples.add(triple);
            this.size.incrementAndGet();
            this.timer.notifyAdd(this.ruleModule);
            if (this.occupation.incrementAndGet() >= this.bufferSize) {
                this.occupation.set(0);
                for (final BufferListener bufferListener : this.bufferListeners) {
                    bufferListener.bufferFull();
                }
            }

        } catch (final Exception e) {
            LOGGER.error("", e);
        } finally {
            this.rwlock.writeLock().unlock();
        }
    }

    @Override
    public void addAll(final Collection<Triple> triples) {
        try {
            this.rwlock.writeLock().lock();
            this.triples.addAll(triples);
            this.timer.notifyAdd(this.ruleModule);
            this.size.addAndGet(triples.size());
            this.occupation.set(this.size() % (int) this.bufferSize);
            for (int i = 0; i < this.size() / this.bufferSize; i++) {
                for (final BufferListener bufferListener : this.bufferListeners) {
                    bufferListener.bufferFull();
                }
            }

        } catch (final Exception e) {
            LOGGER.error("", e);
        } finally {
            this.rwlock.writeLock().unlock();
        }
    }

    @Override
    public TripleStore clear(final long triplesToRead) {
        if (triplesToRead == 0 || this.triples.isEmpty()) {
            return new VerticalPartioningTripleStore();
        }
        TripleStore ts = null;
        try {
            this.rwlock.writeLock().lock();

            ts = new VerticalPartioningTripleStore();

            int read = 0;
            Triple triple = null;
            while (this.triples.peek() != null && read < triplesToRead) {
                triple = this.triples.poll();
                ts.add(triple);
                read++;
            }
            this.size.addAndGet(-read);

        } catch (final Exception e) {
            LOGGER.error("", e);
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

            ts = new VerticalPartioningTripleStore();

            int read = 0;
            Triple triple = null;
            while (this.triples.peek() != null && read < this.bufferSize) {
                triple = this.triples.poll();
                ts.add(triple);
                read++;
            }
            this.size.addAndGet(-read);

        } catch (final Exception e) {
            LOGGER.error("", e);
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
            LOGGER.error("", e);
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
        return this.occupation.get();
    }

    @Override
    public boolean isEmpty() {
        return this.size.get() == 0;
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
            if (!(this.occupation.get() > this.bufferSize)) {
                for (final BufferListener bufferListener : this.bufferListeners) {
                    bufferListener.bufferFull();
                }
            }
        } catch (final Exception e) {
            LOGGER.error("", e);
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
        try {
            this.rwlock.writeLock().lock();
            this.occupation.addAndGet(-triples);
        } catch (final Exception e) {
            LOGGER.error("", e);
        } finally {
            this.rwlock.writeLock().unlock();
        }
    }
}