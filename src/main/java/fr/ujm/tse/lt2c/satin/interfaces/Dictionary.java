package fr.ujm.tse.lt2c.satin.interfaces;

/**
 * @author Jules Chevalier
 * 
 *         Interface for dictionary
 *         Stores axioms as strings and gives them an unique id
 *         No duplicates
 * 
 */
public interface Dictionary {

    /**
     * @param s
     * @return the id of the axiom named s, just added in the dictionary
     *         If the axiom was already present, it just return the id
     */
    long add(String s);

    /**
     * @param index
     * @return the name of the axiom indexed with index
     */
    String get(long index);

    /**
     * @param s
     * @return the id of the axiom named s
     */
    long get(String s);

    /**
     * @return the number of axioms
     */
    long size();

    /**
     * @param c
     * @return the axiom name without the url, or "BLANKNODE" if it is one
     */
    String printAxiom(String c);

    /**
     * @param t
     * @return the triple printed thanks to printAxiom
     * @see #printaxiom(String)
     * @see Triple
     */
    String printTriple(Triple t);

    /**
     * @return the entire dictionary in a String
     */
    String printDico();

}
