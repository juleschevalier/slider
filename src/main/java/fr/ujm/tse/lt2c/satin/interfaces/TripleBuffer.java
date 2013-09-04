package fr.ujm.tse.lt2c.satin.interfaces;

import java.util.Collection;

public interface TripleBuffer{
	
	public void add(Triple triple);
	
	public Collection<Triple> clear();
	
	public void addBufferListener(BufferListener bufferListener);
	
	public Collection<Triple> close();

}
