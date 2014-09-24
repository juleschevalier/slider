package fr.ujm.tse.lt2c.satin.slider.dictionary;

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

import fr.ujm.tse.lt2c.satin.slider.interfaces.Dictionary;

/**
 * @author Jules Chevalier
 * 
 */
public abstract class AbstractDictionary implements Dictionary {

    public static long allDifferent = 0;
    public static long allDisjointClasses = 0;
    public static long allValuesFrom = 0;
    public static long annotationProperty = 0;
    public static long assertionProperty = 0;
    public static long asymetricProperty = 0;
    public static long classOwl = 0;
    public static long classRdfs = 0;
    public static long complementOf = 0;
    public static long differentFrom = 0;
    public static long disjoinWith = 0;
    public static long distinctmembers = 0;
    public static long equivalentClass = 0;
    public static long equivalentProperty = 0;
    public static long functionalProperty = 0;
    public static long hasKey = 0;
    public static long hasValue = 0;
    public static long intersectionOf = 0;
    public static long inverseFunctionalProperty = 0;
    public static long inverseOf = 0;
    public static long irreflexiveProperty = 0;
    public static long maxCardinality = 0;
    public static long maxQualifiedCardinality = 0;
    public static long member = 0;
    public static long nothing = 0;
    public static long onClass = 0;
    public static long oneOf = 0;
    public static long onProperty = 0;
    public static long propertyChainAxiom = 0;
    public static long propertyDisjointWith = 0;
    public static long sameAs = 0;
    public static long someValuesFrom = 0;
    public static long sourceIndividual = 0;
    public static long symetricProperty = 0;
    public static long targetIndividual = 0;
    public static long targetValue = 0;
    public static long thing = 0;
    public static long transitiveProperty = 0;
    public static long unionOf = 0;
    public static long domain = 0;
    public static long range = 0;
    public static long subClassOf = 0;
    public static long subPropertyOf = 0;
    public static long type = 0;
    public static long ressource = 0;
    public static long property = 0;
    public static long containerMembershipProperty = 0;
    public static long literal = 0;
    public static long datatype = 0;

    @Override
    public String printAxiom(final String c) {
        String axiom = c;
        /* Replace integers, strings... by their value */
        axiom = axiom.replaceAll("(\".*\")\\^\\^.*", "$1");

        /* Cut the URL, keep the name */
        if (axiom.split("#").length > 1) {
            axiom = axiom.split("#")[1];
        }

        /* Replace all the blank nodes by "BLANKNODE" */
        axiom = axiom.replaceAll("-?[0-9a-z]+:[0-9a-z]+:-?[0-9a-z]+", "BLANKNODE");
        axiom = axiom.replaceAll("-?[0-9a-z]+:[0-9a-z]+:-?[0-9a-z]+", "BLANKNODE");

        return axiom;
    }

    public void initialize() {
        /*
         * All the prefixes come from prefix.cc
         */
        domain = this.add("http://www.w3.org/2000/01/rdf-schema#domain");
        range = this.add("http://www.w3.org/2000/01/rdf-schema#range");
        type = this.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        subClassOf = this.add("http://www.w3.org/2000/01/rdf-schema#subClassOf");
        subPropertyOf = this.add("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");
        equivalentClass = this.add("http://www.w3.org/2002/07/owl#equivalentClass");
        equivalentProperty = this.add("http://www.w3.org/2002/07/owl#equivalentProperty");
        sameAs = this.add("http://www.w3.org/2002/07/owl#sameAs");
        inverseOf = this.add("http://www.w3.org/2002/07/owl#inverseOf");
        propertyDisjointWith = this.add("http://www.w3.org/2002/07/owl#propertyDisjointWith");
        differentFrom = this.add("http://www.w3.org/2002/07/owl#differentFrom");
        allDifferent = this.add("http://www.w3.org/2002/07/owl#AllDifferent");
        allDisjointClasses = this.add("http://www.w3.org/2002/07/owl#allDisjointClasses");
        allValuesFrom = this.add("http://www.w3.org/2002/07/owl#allValuesFrom");
        annotationProperty = this.add("http://www.w3.org/2002/07/owl#AnnotationProperty");
        assertionProperty = this.add("http://www.w3.org/2002/07/owl#assertionProperty");
        asymetricProperty = this.add("http://www.w3.org/2002/07/owl#asymetricProperty");
        classOwl = this.add("http://www.w3.org/2002/07/owl#Class");
        classRdfs = this.add("http://www.w3.org/2000/01/rdf-schema#");
        complementOf = this.add("http://www.w3.org/2002/07/owl#complementOf");
        disjoinWith = this.add("http://www.w3.org/2002/07/owl#disjointWith");
        distinctmembers = this.add("http://www.w3.org/2002/07/owl#distinctMembers");
        functionalProperty = this.add("http://www.w3.org/2002/07/owl#functionalProperty");
        hasKey = this.add("http://www.w3.org/2002/07/owl#hasKey");
        hasValue = this.add("http://www.w3.org/2002/07/owl#hasValue");
        intersectionOf = this.add("http://www.w3.org/2002/07/owl#intersectionOf");
        inverseFunctionalProperty = this.add("http://www.w3.org/2002/07/owl#inverseFunctionalProperty");
        irreflexiveProperty = this.add("http://www.w3.org/2002/07/owl#irreflexiveProperty");
        maxCardinality = this.add("http://www.w3.org/2002/07/owl#maxCardinality");
        maxQualifiedCardinality = this.add("http://www.w3.org/2002/07/owl#maxQualifiedCardinality");
        member = this.add("http://www.w3.org/2002/07/owl#member");
        nothing = this.add("http://www.w3.org/2002/07/owl#nothing");
        onClass = this.add("http://www.w3.org/2002/07/owl#onClass");
        onProperty = this.add("http://www.w3.org/2002/07/owl#onProperty");
        oneOf = this.add("http://www.w3.org/2002/07/owl#oneOf");
        propertyChainAxiom = this.add("http://www.w3.org/2002/07/owl#propertyChainAxiom");
        someValuesFrom = this.add("http://www.w3.org/2002/07/owl#someValuesFrom");
        sourceIndividual = this.add("http://www.w3.org/2002/07/owl#sourceIndividual");
        symetricProperty = this.add("http://www.w3.org/2002/07/owl#symetricProperty");
        targetIndividual = this.add("http://www.w3.org/2002/07/owl#targetIndividual");
        targetValue = this.add("http://www.w3.org/2002/07/owl#targetValue");
        thing = this.add("http://www.w3.org/2002/07/owl#Thing");
        transitiveProperty = this.add("http://www.w3.org/2002/07/owl#TransitiveProperty");
        unionOf = this.add("http://www.w3.org/2002/07/owl#unionOf");
        ressource = this.add("http://www.w3.org/2000/01/rdf-schema#ressource");
        property = this.add("http://www.w3.org/2000/01/rdf-schema#property");
        containerMembershipProperty = this.add("http://www.w3.org/2000/01/rdf-schema#containerMembershipProperty");
        literal = this.add("http://www.w3.org/2000/01/rdf-schema#Literal");
        datatype = this.add("http://www.w3.org/2000/01/rdf-schema#Datatype");
    }
}
