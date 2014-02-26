package fr.ujm.tse.lt2c.satin.interfaces;

import com.hp.hpl.jena.rdf.model.Model;

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

    void parse(Model model);

}