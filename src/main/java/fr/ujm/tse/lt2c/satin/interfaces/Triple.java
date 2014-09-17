package fr.ujm.tse.lt2c.satin.interfaces;

/**
 * Interface for triple representation
 * 
 * @author Jules Chevalier
 * 
 */
public interface Triple {

    /**
     * @return the subject of the triple
     */
    long getSubject();

    /**
     * @return the predicate of the triple
     */
    long getPredicate();

    /**
     * @return the object of the triple
     */
    long getObject();

}