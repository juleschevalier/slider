package fr.ujm.tse.lt2c.satin.buffer;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

/**
 * Triple Buffer managing concurrency.
 * 
 * New triples gone in the main buffer until it's full, then the buffers are
 * switched and the fullbuffer event is thrown.
 * 
 * @author Jules Chevalier
 * @see TripleBuffer
 */
@Deprecated
public class TripleBufferLock implements TripleBuffer {

    /* Limit of the main buffer (adding the last triple calls bufferfull) */
    static final long BUFFER_SIZE = 100;

    private static Logger logger = Logger.getLogger(TripleBufferLock.class);

    TripleStore mainBuffer;
    TripleStore secondaryBuffer;
    ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
    Collection<BufferListener> bufferListeners;

    String debugName;

    long lastFlush;

    /**
     * Constructor
     */
    public TripleBufferLock() {
        this.mainBuffer = new VerticalPartioningTripleStoreRWLock();
        this.secondaryBuffer = new VerticalPartioningTripleStoreRWLock();
        this.bufferListeners = new HashSet<>();
        this.lastFlush = System.nanoTime();
    }

    @Override
    public boolean add(final Triple triple) {
        boolean success = false;
        try {
            this.rwlock.writeLock().lock();
            if (this.mainBuffer.size() < BUFFER_SIZE) {
                this.mainBuffer.add(triple);
                success = true;
            } else {
                logger.trace(this.hashCode() + " is full");
                if (this.secondaryBuffer.isEmpty()) {
                    logger.trace(this.hashCode() + " switch buffers");
                    this.switchBuffers();
                    for (final BufferListener bufferListener : this.bufferListeners) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Buffer really full");
                        }
                        bufferListener.bufferFull();
                    }
                    this.mainBuffer.add(triple);
                    success = true;
                }

            }
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.rwlock.writeLock().unlock();
        }
        if (!success && logger.isTraceEnabled()) {
            logger.trace(this.hashCode() + "Add failed");
        }
        return success;
    }

    /**
     * Switch the main and the secondary buffers
     */
    private void switchBuffers() {
        final TripleStore temp = this.mainBuffer;
        this.mainBuffer = this.secondaryBuffer;
        this.secondaryBuffer = temp;
    }

    @Override
    public TripleStore clear() {
        if ((this.mainBufferOccupation() + this.secondaryBufferOccupation()) == 0) {
            logger.warn(this.debugName + " clear an empty buffer");
        }

        this.rwlock.writeLock().lock();
        if (this.secondaryBuffer.isEmpty()) {
            this.switchBuffers();
        }
        TripleStore temp = null;
        try {
            temp = this.secondaryBuffer;
            this.secondaryBuffer = new VerticalPartioningTripleStoreRWLock();
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.lastFlush = System.nanoTime();
            synchronized (this) {
                this.notifyAll();
            }
            this.rwlock.writeLock().unlock();
        }
        return temp;
    }

    @Override
    public void addBufferListener(final BufferListener bufferListener) {
        this.bufferListeners.add(bufferListener);
    }

    /**
     * @deprecated
     */
    @Override
    @Deprecated
    public TripleStore flush() {
        final TripleStore temp = this.mainBuffer;
        this.lastFlush = System.nanoTime();
        this.mainBuffer = new VerticalPartioningTripleStoreRWLock();
        return temp;
    }

    @Override
    public Collection<Triple> getCollection() {
        return this.mainBuffer.getAll();
    }

    @Override
    public long getBufferLimit() {
        return BUFFER_SIZE;
    }

    @Override
    public long mainBufferOccupation() {
        return this.mainBuffer.size();
    }

    @Override
    public long secondaryBufferOccupation() {

        return this.secondaryBuffer.size();
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
        this.rwlock.writeLock().lock();
        try {
            if (!this.mainBuffer.isEmpty() && this.secondaryBuffer.isEmpty()) {
                logger.trace(this.hashCode() + " switch buffers because of timeout");
                this.switchBuffers();
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
    public long getOccupation() {
        return this.mainBufferOccupation() + this.secondaryBufferOccupation();
    }
}