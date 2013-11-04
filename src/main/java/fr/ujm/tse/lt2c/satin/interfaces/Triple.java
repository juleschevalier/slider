package fr.ujm.tse.lt2c.satin.interfaces;

/**
 * @author Jules Chevalier
 * Interface for triple representation
 *
 */
public interface Triple {

	
	/**
	 * @return the subject of the triple
	 */
	public abstract long getSubject();

	/**
	 * @return the predicate of the triple
	 */
	public abstract long getPredicate();

	/**
	 * @return the object of the triple
	 */
	public abstract long getObject();
	
	public abstract int hashCode();
	
	public abstract boolean equals(Object obj);

}