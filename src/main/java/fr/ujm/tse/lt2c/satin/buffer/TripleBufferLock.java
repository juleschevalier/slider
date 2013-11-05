package fr.ujm.tse.lt2c.satin.buffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

public class TripleBufferLock implements TripleBuffer {

        TripleStore mainBuffer;
        TripleStore secondaryBuffer;
        ReentrantReadWriteLock mainLock = new ReentrantReadWriteLock();
        ReentrantReadWriteLock secondaryLock = new ReentrantReadWriteLock();
        Collection<BufferListener> bufferListeners; // Registered listeners

        static final long BUFFER_LIMIT = 100; // Limit of the main buffer

        public TripleBufferLock() {
                this.mainBuffer = new VerticalPartioningTripleStoreRWLock();
                // this.buffer1_size = 0;
                this.secondaryBuffer = new VerticalPartioningTripleStoreRWLock();
                // this.buffer2_size = 0;
                this.bufferListeners = new ArrayList<>();
        }

        @Override
        public void add(final Triple triple) {
                mainLock.writeLock().lock();
                try {
                        this.mainBuffer.add(triple);
                        if (this.mainBuffer.size() >= BUFFER_LIMIT) {
                                // System.out.println("Buffer full");
                                if (this.secondaryBuffer.isEmpty()) {
                                        // System.out.println("Buffer switch");
                                        TripleStore temp = mainBuffer;
                                        mainBuffer = secondaryBuffer;
                                        secondaryBuffer = temp;
                                        for (BufferListener bufferListener : bufferListeners) {
                                                bufferListener.bufferFull();
                                        }
                                }
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                } finally {
                        mainLock.writeLock().unlock();
                }
        }

        @Override
        public TripleStore clear() {
                mainLock.writeLock().lock();
                TripleStore temp = null;
                try {
                        temp = secondaryBuffer;
                        secondaryBuffer = new VerticalPartioningTripleStoreRWLock();
                } catch (Exception e) {
                        e.printStackTrace();
                } finally {
                        mainLock.writeLock().unlock();
                }
                return temp;
        }

        @Override
        public void addBufferListener(BufferListener bufferListener) {
                this.bufferListeners.add(bufferListener);
        }

        @Override
        public TripleStore flush() {
                TripleStore temp = this.mainBuffer;
                mainBuffer = new VerticalPartioningTripleStoreRWLock();
                return temp;
        }

        @Override
        public Collection<Triple> getCollection() {
                return mainBuffer.getAll();
        }

        @Override
        public long getBufferLimit() {

                return BUFFER_LIMIT;
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

}