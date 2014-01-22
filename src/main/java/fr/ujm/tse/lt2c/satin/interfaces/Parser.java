package fr.ujm.tse.lt2c.satin.interfaces;

/**
 * @author jules
 * 
 *         Interface for an ontology parser
 */
public interface Parser {

    /**
     * Parse and "save" the ontology's triples in the TripleStore
     * 
     * @param fileInput
     */
    void parse(String fileInput);

    @Override
    int hashCode();

    @Override
    boolean equals(Object obj);

}