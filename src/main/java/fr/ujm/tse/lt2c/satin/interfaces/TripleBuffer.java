package fr.ujm.tse.lt2c.satin.interfaces;

import java.util.Collection;

/**
 * @author jules
 * 
 *         Interface for triple buffer
 */
public interface TripleBuffer {

	/**
	 * @param triple
	 *            Add a triple to the buffer.
	 *            Notifies subcsribers of "bufferfull" if needed
	 *            
	 * @return true if the insertion succeed
	 */
	public boolean add(Triple triple);

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
	public TripleStore clear();

	/**
	 * @param bufferListener
	 *            which want to listen for buffer events
	 */
	public void addBufferListener(BufferListener bufferListener);

	public Collection<BufferListener> getBufferListeners();

	/**
	 * Flush all remaining triples in this buffer. Calls this method when
	 * terminating the process
	 * 
	 * @return
	 */
	public TripleStore flush();

	/**
	 * 
	 * @return the size limit of the main buffer
	 */
	public long getBufferLimit();

	/**
	 * 
	 * @return all triples in the main buffer
	 */
	public Collection<Triple> getCollection();

	/**
	 * Notifies all subscribers that the buffer is full
	 */
	public void sendFullBuffer();

	public long mainBufferOccupation();

	public long secondaryBufferOccupation();
	
	public long getLastFlush();

}
