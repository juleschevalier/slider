package fr.ujm.tse.lt2c.satin.buffer;

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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

/**
 * Concurrent implementation of {@link interfaces.TripleBuffer} using a ConcurrentLinkedQueue as buffer
 * 
 * @author Jules Chevalier
 * @see TripleBuffer
 */
public class QueuedTripleBufferLock implements TripleBuffer {
    private static Logger logger = Logger.getLogger(QueuedTripleBufferLock.class);

    /* Limit of the buffer (adding the last triple calls bufferfull) */
    private final long bufferSize;

    Queue<Triple> triples;
    ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
    Collection<BufferListener> bufferListeners;
    AtomicInteger currentBuffer;

    String debugName;

    /**
     * Constructor
     */
    public QueuedTripleBufferLock(final long bufferSize) {
        this.triples = new ConcurrentLinkedQueue<>();
        this.bufferListeners = new HashSet<>();
        this.currentBuffer = new AtomicInteger();
        this.bufferSize = bufferSize;
    }

    @Override
    public void add(final Triple triple) {
        try {
            this.rwlock.writeLock().lock();

            this.triples.add(triple);
            if (this.currentBuffer.incrementAndGet() >= this.bufferSize) {
                this.currentBuffer.set(0);
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
    public void addAll(final Collection<Triple> triples) {
        try {
            this.rwlock.writeLock().lock();
            this.triples.addAll(triples);
            for (final BufferListener bufferListener : this.bufferListeners) {
                for (int i = 0; i < (triples.size() / this.bufferSize); i++) {
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
    public TripleStore clear() {
        TripleStore ts = null;
        try {
            this.rwlock.writeLock().lock();
            ts = new VerticalPartioningTripleStoreRWLock();

            int i = 0;
            Triple triple = this.triples.poll();
            while ((triple != null) && (i++ < this.bufferSize)) {
                ts.add(triple);
                if (i < this.bufferSize) {
                    triple = this.triples.poll();
                } else {
                    triple = null;
                }
            }
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
        return this.triples;
    }

    @Override
    public long getBufferLimit() {
        return this.bufferSize;
    }

    @Override
    public long getOccupation() {
        return this.triples.size();
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
}