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

    /* Limit of the main buffer (adding the last triple calls bufferfull) */
    public long bufferSize;

    private static Logger logger = Logger.getLogger(QueuedTripleBufferLock.class);

    Queue<Triple> tripleQueue;
    ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
    Collection<BufferListener> bufferListeners;
    AtomicInteger currentBuffer;

    String debugName;

    long lastFlush;

    /**
     * Constructor
     */
    public QueuedTripleBufferLock(final long bufferSize) {
        this.tripleQueue = new ConcurrentLinkedQueue<>();
        this.bufferListeners = new HashSet<>();
        this.lastFlush = System.nanoTime();
        this.currentBuffer = new AtomicInteger();
        this.bufferSize = bufferSize;
    }

    @Override
    public boolean add(final Triple triple) {
        boolean success = false;
        try {
            this.rwlock.writeLock().lock();

            success = this.addNoLock(triple);

        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.rwlock.writeLock().unlock();
        }
        return success;
    }

    @Override
    public boolean addAll(final Collection<Triple> triples) {
        boolean success = true;
        try {
            this.rwlock.writeLock().lock();
            for (final Triple triple : triples) {
                if (!this.addNoLock(triple)) {
                    success = false;
                }
            }

        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.rwlock.writeLock().unlock();
        }
        return success;
    }

    private boolean addNoLock(final Triple triple) {
        boolean success;
        success = this.tripleQueue.add(triple);
        if (this.currentBuffer.incrementAndGet() > this.bufferSize) {
            for (final BufferListener bufferListener : this.bufferListeners) {
                bufferListener.bufferFull();
            }
            this.currentBuffer.set(0);
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
            Triple triple = this.tripleQueue.poll();
            while ((triple != null) && (i++ < this.bufferSize)) {
                ts.add(triple);
                triple = this.tripleQueue.poll();
            }
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.lastFlush = System.nanoTime();
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
        return this.tripleQueue;
    }

    @Override
    public long getBufferLimit() {
        return this.bufferSize;
    }

    @Override
    public long getOccupation() {
        return this.tripleQueue.size();
    }

    @Override
    public Collection<BufferListener> getBufferListeners() {
        return this.bufferListeners;
    }

    @Override
    public long getLastFlush() {
        return this.lastFlush;
    }

    @Override
    public void sendFullBuffer() {
        try {
            this.rwlock.writeLock().lock();
            if (!(this.tripleQueue.size() > this.bufferSize)) {
                logger.trace(this.hashCode() + " switch buffers because of timeout");
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