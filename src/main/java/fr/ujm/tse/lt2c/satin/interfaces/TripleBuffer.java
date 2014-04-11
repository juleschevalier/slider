package fr.ujm.tse.lt2c.satin.interfaces;

import java.util.Collection;

/**
 * Interface for FIFO triple buffer
 * 
 * @author jules
 * 
 */
public interface TripleBuffer {

    /**
     * Add a triple to the buffer.
     * Notifies subcsribers of "bufferfull" if needed
     * 
     * @param triple
     * @return true if the insertion succeed
     * @see Triple
     */
    void add(Triple triple);

    /**
     * @return the bufferSize first triples inserted in a TripleStore
     * @See TripleStore
     */
    TripleStore clear();

    /**
     * Add a listener to the buffer's events
     * 
     * @param bufferListener
     * @see BufferListener
     */
    void addBufferListener(BufferListener bufferListener);

    /**
     * @return the buffer listeners in a collection
     * @see BufferListener
     */
    Collection<BufferListener> getBufferListeners();

    /**
     * @return the limit size when bufferFull() is sent
     */
    long getBufferLimit();

    /**
     * @return all triples in the main buffer
     * @see Triple
     */
    Collection<Triple> getCollection();

    /**
     * Notifies all subscribers that the buffer is full
     */
    void sendFullBuffer();

    /**
     * For debugging, set the name used by logger
     * 
     * @param name
     */
    void setDebugName(String name);

    /**
     * @return the name used by logger for debugging
     */
    String getDebugName();

    /**
     * @return the number of triples stored
     */
    long getOccupation();

    /**
     * Add all the triples to the buffer.
     * Notifies subcsribers of "bufferfull" if needed
     * 
     * @param triples
     * @return true if the insertion succeed
     * @see Triple
     */
    void addAll(Collection<Triple> triples);

}
