package fr.ujm.tse.lt2c.satin.interfaces;

/**
 * @author jules
 * 
 * Interface for triple buffer
 */
public interface TripleBuffer{
	
	/**
	 * @param triple
	 * Add a triple to the buffer.
	 * Notifies subcsribers of "bufferfull" if needed
	 */
	public void add(Triple triple);
	
	/**
	 * @return The triples stored in the buffer full
	 */
	public TripleStore clear();
	
	/**
	 * @param bufferListener which want to listen for buffer events
	 */
	public void addBufferListener(BufferListener bufferListener);
	
	/**
	 * @return The triples in the not full buffer
	 */
	public TripleStore close();

}
