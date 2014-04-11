package fr.ujm.tse.lt2c.satin.interfaces;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author jules
 * 
 *         Interface for an ontology parser
 */
public interface Parser {

    /**
     * Parse and "save" the ontology's triples from a file in the TripleStore
     * 
     * @param fileInput
     */
    void parse(String fileInput);

    /**
     * Parse and "save" the ontology's triples from a model in the TripleStore
     * 
     * @param model
     * @see Model
     */
    void parse(Model model);

}