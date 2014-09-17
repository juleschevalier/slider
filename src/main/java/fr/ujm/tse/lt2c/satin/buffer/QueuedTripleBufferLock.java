package fr.ujm.tse.lt2c.satin.buffer;

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
import fr.ujm.tse.lt2c.satin.rules.Rule;
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

    private final Queue<Triple> triples;
    private final ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
    private final Collection<BufferListener> bufferListeners;
    private final AtomicInteger currentBuffer;
    private final BufferTimer timer;
    private final Rule rule;

    String debugName;

    /**
     * Constructor
     */
    public QueuedTripleBufferLock(final long bufferSize, final BufferTimer timer, final Rule rule) {
        this.triples = new ConcurrentLinkedQueue<>();
        this.bufferListeners = new HashSet<>();
        this.currentBuffer = new AtomicInteger();
        this.bufferSize = bufferSize;
        this.timer = timer;
        this.rule = rule;
    }

    @Override
    public void add(final Triple triple) {
        try {
            this.rwlock.writeLock().lock();
            this.triples.add(triple);
            this.timer.notifyAdd(this.rule);
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
                for (int i = 0; i < triples.size() / this.bufferSize; i++) {
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
            while (triple != null && i++ < this.bufferSize) {
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
        long size = -1;
        try {
            // this.rwlock.readLock().lock();
            size = this.triples.size();
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            // this.rwlock.readLock().unlock();
        }
        return size;
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