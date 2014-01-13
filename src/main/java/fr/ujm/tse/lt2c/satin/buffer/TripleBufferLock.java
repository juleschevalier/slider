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
 * @author Jules Chevalier
 * 
 */
public class TripleBufferLock implements TripleBuffer {

	private static Logger logger = Logger.getLogger(TripleBufferLock.class);

	TripleStore mainBuffer;
	TripleStore secondaryBuffer;
	ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
	Collection<BufferListener> bufferListeners;

	String debugName;

	@Override
	public String getDebugName() {
		return debugName;
	}

	@Override
	public void setDebugName(String debugName) {
		this.debugName = debugName;
	}

	long lastFlush;

	// Limit of the main buffer
	static final long BUFFER_SIZE = 100;

	public TripleBufferLock() {
		this.mainBuffer = new VerticalPartioningTripleStoreRWLock();
		this.secondaryBuffer = new VerticalPartioningTripleStoreRWLock();
		this.bufferListeners = new HashSet<>();
		this.lastFlush = System.nanoTime();
	}

	@Override
	public boolean add(final Triple triple) {
		rwlock.writeLock().lock();
		boolean success = false;
		try {
			if (this.mainBuffer.size() < BUFFER_SIZE) {
				this.mainBuffer.add(triple);
				success = true;
			} else {
				logger.trace(this.hashCode() + " is full");
				if (this.secondaryBuffer.isEmpty()) {
					logger.trace(this.hashCode() + " switch buffers");
					switchBuffers();
					for (BufferListener bufferListener : bufferListeners) {
						if (logger.isTraceEnabled()) {
							logger.trace("Buffer really full");
						}
						bufferListener.bufferFull();
					}
					this.mainBuffer.add(triple);
					success = true;
				}
			}
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			rwlock.writeLock().unlock();
		}
		if (!success) {
			if (logger.isTraceEnabled()) {
				logger.trace(this.hashCode() + "Add failed");
			}
		}
		return success;
	}

	private void switchBuffers() {
		TripleStore temp = mainBuffer;
		mainBuffer = secondaryBuffer;
		secondaryBuffer = temp;
	}

	@Override
	public TripleStore clear() {
		if (mainBufferOccupation() + secondaryBufferOccupation() == 0) {
			logger.warn(this.debugName + " clear an empty buffer");
		}

		rwlock.writeLock().lock();
		if (secondaryBuffer.isEmpty()) {
			switchBuffers();
		}
		TripleStore temp = null;
		try {
			temp = secondaryBuffer;
			secondaryBuffer = new VerticalPartioningTripleStoreRWLock();
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			this.lastFlush = System.nanoTime();
			rwlock.writeLock().unlock();
		}
		return temp;
	}

	@Override
	public void addBufferListener(BufferListener bufferListener) {
		this.bufferListeners.add(bufferListener);
	}

	/**
	 * @deprecated
	 */
	@Override
	@Deprecated
	public TripleStore flush() {
		TripleStore temp = this.mainBuffer;
		this.lastFlush = System.nanoTime();
		mainBuffer = new VerticalPartioningTripleStoreRWLock();
		return temp;
	}

	@Override
	public Collection<Triple> getCollection() {
		return mainBuffer.getAll();
	}

	@Override
	public long getBufferLimit() {
		return BUFFER_SIZE;
	}

	@Override
	public long mainBufferOccupation() {
		return mainBuffer.size();
	}

	@Override
	public long secondaryBufferOccupation() {

		return secondaryBuffer.size();
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
		rwlock.writeLock().lock();
		try {
			if (!this.mainBuffer.isEmpty() && this.secondaryBuffer.isEmpty()) {
				logger.trace(this.hashCode() + " switch buffers because of timeout");
				switchBuffers();
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
}