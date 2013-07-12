package fr.ujm.tse.lt2c.satin.interfaces;

import java.util.Collection;

public interface TripleStore {
	
	public void add(Triple t);
	
	public void addAll(Collection<Triple> t);
	
	public Collection<Triple> getAll();
	
	public Collection<Triple> getbySubject(long s);
	
	public Collection<Triple> getbyPredicate(long p);
	
	public Collection<Triple> getbyObject(long o);
	
	public int hashCode();
	
	public boolean equals(Object obj);
	
	public void writeToFile(String file, Dictionnary dictionnary);

}
