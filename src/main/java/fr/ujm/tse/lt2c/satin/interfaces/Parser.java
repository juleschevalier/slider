package fr.ujm.tse.lt2c.satin.interfaces;

/*
 * #%L
 * SLIDeR
 * %%
 * Copyright (C) 2014 Université Jean Monnet, Saint Etienne
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Collection;

import com.hp.hpl.jena.rdf.model.Model;

import fr.ujm.tse.lt2c.satin.reasoner.ReasonerStreamed;

/**
 * @author Jules Chevalier
 * 
 *         Interface for an ontology parser
 */
public interface Parser {

    /**
     * Parse and "save" the ontology's triples from a file in the TripleStore
     * 
     * @param fileInput
     * @return A collection of the parsed triples
     * @see Triple
     */
    Collection<Triple> parse(String fileInput);

    /**
     * Parse, "save" the ontology's triples from a file in the TripleStore and send them for reasoning
     * 
     * @param fileInput
     * @return The number of triples parsed
     * @see Triple
     */
    int parseStream(String fileInput, ReasonerStreamed reasoner);

    /**
     * Parse and "save" the ontology's triples from a model in the TripleStore
     * 
     * @param model
     * @return A collection of the parsed triples
     * @see Model
     * @see Triple
     */
    Collection<Triple> parse(Model model);

}