package fr.ujm.tse.lt2c.satin.interfaces;


/**
 * @author Jules Chevalier
 *
 */
public interface Dictionnary {
	
	public abstract long add(String s);
	
	public abstract String get(long index);
	
	public abstract long get(String s);
	
	public abstract long size();

	public abstract int hashCode();

	public abstract boolean equals(Object obj);
	
	public String printTriple(Triple t);
	
}
