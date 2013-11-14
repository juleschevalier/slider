package fr.ujm.tse.lt2c.satin.interfaces;

/**
 * @author Jules Chevalier
 * 
 *         Interface for concepts dictionary
 *         Allow to match concepts names with integers used for computations
 * 
 */
public interface Dictionary {

	/**
	 * @param s
	 * @return the id of the concept named s, just added in the dictionary
	 *         If the concept was already present, it just return the id
	 */
	public abstract long add(String s);

	/**
	 * @param index
	 * @return the name of the concept indexed with index
	 */
	public abstract String get(long index);

	/**
	 * @param s
	 * @return the id of the concept named s
	 */
	public abstract long get(String s);

	/**
	 * @return the number of concepts
	 */
	public abstract long size();

	/**
	 * @param c
	 * @return the concept name without the url, or "BLANKNODE" if it is one
	 */
	public String printConcept(String c);

	/**
	 * @param t
	 * @return the triple printed thanks to printConcept
	 * @see printConcept
	 */
	public String printTriple(Triple t);

	public abstract int hashCode();

	public abstract boolean equals(Object obj);

}
