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
     * @see String
     * @see Triple
     */
    String printTriple(Triple t);

    /**
     * @return the entire dictionary in a String
     */
    String printDico();

}
