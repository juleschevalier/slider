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
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

/**
 * 
 * @author Jules Chevalier
 * @see TripleBuffer
 */
public class QueuedTripleBufferLock implements TripleBuffer {

    /* Limit of the buffer (adding the last triple calls bufferfull) */
    private final long bufferSize;

    private static Logger logger = Logger.getLogger(QueuedTripleBufferLock.class);

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
    public boolean add(final Triple triple) {
        boolean success = false;
        try {
            this.rwlock.writeLock().lock();

            success = this.triples.add(triple);
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
        return success;
    }

    @Override
    public boolean addAll(final Collection<Triple> triples) {
        final boolean success = true;
        try {
            this.rwlock.writeLock().lock();
            triples.addAll(triples);
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
        return success;
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