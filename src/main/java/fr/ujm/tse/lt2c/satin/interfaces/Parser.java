package fr.ujm.tse.lt2c.satin.interfaces;

import java.util.Collection;

import com.hp.hpl.jena.rdf.model.Model;

import fr.ujm.tse.lt2c.satin.reasoner.ReasonerStreamed;

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
     * @see Triple
     */
    Collection<Triple> parse(String fileInput);

    /**
     * Parse, "save" the ontology's triples from a file in the TripleStore and send them for reasoning
     * 
     * @param fileInput
     * @see Triple
     */
    int parseStream(String fileInput, ReasonerStreamed reasoner);

    /**
     * Parse and "save" the ontology's triples from a model in the TripleStore
     * 
     * @param model
     * @see Model
     * @see Triple
     */
    Collection<Triple> parse(Model model);

}