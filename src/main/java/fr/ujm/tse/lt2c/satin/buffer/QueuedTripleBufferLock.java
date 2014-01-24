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

    private static Logger logger = Logger.getLogger(QueuedTripleBufferLock.class);

    Queue<Triple> tripleQueue;
    AtomicInteger size;
    ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
    Collection<BufferListener> bufferListeners;
    AtomicInteger currentBuffer;

    String debugName;

    long lastFlush;

    /* Limit of the main buffer (adding the last triple calls bufferfull) */
    static final long BUFFER_SIZE = 100;

    /**
     * Constructor
     */
    public QueuedTripleBufferLock() {
        this.tripleQueue = new ConcurrentLinkedQueue<>();
        this.bufferListeners = new HashSet<>();
        this.lastFlush = System.nanoTime();
        this.currentBuffer = new AtomicInteger();
    }

    @Override
    public boolean add(final Triple triple) {
        boolean success = false;
        try {
            rwlock.writeLock().lock();

            success = tripleQueue.add(triple);
            if (this.currentBuffer.incrementAndGet() > BUFFER_SIZE) {
                for (BufferListener bufferListener : bufferListeners) {
                    bufferListener.bufferFull();
                }
                this.currentBuffer.set(0);
            }

        } catch (Exception e) {
            logger.error("", e);
        } finally {
            rwlock.writeLock().unlock();
        }
        return success;
    }

    @Override
    public TripleStore clear() {

        TripleStore ts = null;
        try {
            rwlock.writeLock().lock();
            ts = new VerticalPartioningTripleStoreRWLock();
            for (int i = 0; ((i < tripleQueue.size()) && (i < BUFFER_SIZE)); i++) {
                ts.add(tripleQueue.poll());
            }
            this.currentBuffer.set(0);
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            this.lastFlush = System.nanoTime();
            rwlock.writeLock().unlock();
            synchronized (this) {
                this.notifyAll();
            }
        }
        return ts;
    }

    @Override
    public void addBufferListener(BufferListener bufferListener) {
        this.bufferListeners.add(bufferListener);
    }

    @Override
    public Collection<Triple> getCollection() {
        return this.tripleQueue;
    }

    @Override
    public long getBufferLimit() {
        return BUFFER_SIZE;
    }

    @Override
    public long getOccupation() {
        return tripleQueue.size();
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
            rwlock.writeLock().lock();
            if (!(this.size.get() > BUFFER_SIZE)) {
                logger.trace(this.hashCode() + " switch buffers because of timeout");
                for (BufferListener bufferListener : bufferListeners) {
                    bufferListener.bufferFull();
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            rwlock.writeLock().unlock();
        }

    }

    @Override
    public String getDebugName() {
        return debugName;
    }

    @Override
    public void setDebugName(String debugName) {
        this.debugName = debugName;
    }

    @Override
    public TripleStore flush() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long mainBufferOccupation() {
        // TODO Auto-generated method stub
        return -1;
    }

    @Override
    public long secondaryBufferOccupation() {
        // TODO Auto-generated method stub
        return -1;
    }
}