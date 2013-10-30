package fr.ujm.tse.lt2c.satin.interfaces;

public interface TripleBuffer{
	
	public void add(Triple triple);
	
	public TripleStore clear();
	
	public void addBufferListener(BufferListener bufferListener);
	
	public TripleStore close();

}
