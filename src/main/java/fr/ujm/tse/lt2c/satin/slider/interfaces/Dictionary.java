package fr.ujm.tse.lt2c.satin.slider.interfaces;

import com.hp.hpl.jena.graph.Node;

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

/**
 * 
 * Interface for a dictionnary mapping Nodes (RDF concepts) to Long id
 * 
 * @author Jules Chevalier
 */
public interface Dictionary {

    /**
     * @param s
     * @return Adds the concept represented by s, and returns the id if it
     *         If the Node was already present, just returns the id
     */
    long add(String s);

    /**
     * @param n
     * @return Adds the concept represented by n, and returns the id if it
     *         If the Node was already present, just returns the id
     */
    long add(Node n);

    /**
     * @param index
     * @return the node indexed with index
     */
    Node get(long index);

    /**
     * @param s
     * @return the id of the axiom named s
     */
    long get(String s);

    /**
     * @param n
     * @return the id of the axiom from the Jena Node n
     */
    long get(Node n);

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
     * @see String
     * @see Triple
     */
    String printTriple(Triple t);

    /**
     * @return the entire dictionary in a String
     */
    String printDico();

}
