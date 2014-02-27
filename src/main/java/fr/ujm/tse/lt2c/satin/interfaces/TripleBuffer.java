package fr.ujm.tse.lt2c.satin.interfaces;

import java.util.Collection;

/**
 * Interface for triple buffer
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
    boolean add(Triple triple);

    /**
     * Following operations are performed :
     * <ol>
     * <li>Returns the second full buffer</li>
     * <li>Empty the second buffer</li>
     * 
     * </ol>
     * 
     * @return
     */
    TripleStore clear();

    /**
     * Add a listener to the buffer's events
     * 
     * @param bufferListener
     * @see BufferListener
     */
    void addBufferListener(BufferListener bufferListener);

    Collection<BufferListener> getBufferListeners();

    /**
     * 
     * @return the size limit of the main buffer
     */
    long getBufferLimit();

    /**
     * 
     * @return all triples in the main buffer
     * @see Triple
     */
    Collection<Triple> getCollection();

    /**
     * Notifies all subscribers that the buffer is full
     */
    void sendFullBuffer();

    /**
     * @return the timestamp since the last time the buffer were flushed
     */
    long getLastFlush();

    /**
     * @param name
     */
    void setDebugName(String name);

    /**
     * @return the name for debugging
     */
    String getDebugName();

    long getOccupation();

    boolean addAll(Collection<Triple> triples);

}
