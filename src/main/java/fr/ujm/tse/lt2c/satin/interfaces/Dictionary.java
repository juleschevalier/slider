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
    long add(String s);

    /**
     * @param index
     * @return the name of the concept indexed with index
     */
    String get(long index);

    /**
     * @param s
     * @return the id of the concept named s
     */
    long get(String s);

    /**
     * @return the number of concepts
     */
    long size();

    /**
     * @param c
     * @return the concept name without the url, or "BLANKNODE" if it is one
     */
    String printConcept(String c);

    /**
     * @param t
     * @return the triple printed thanks to printConcept
     * @see #printConcept(String)
     * @see Triple
     */
    String printTriple(Triple t);

    @Override
    int hashCode();

    @Override
    boolean equals(Object obj);

    /**
     * @return the entire dictionary in a String
     */
    String printDico();

}
