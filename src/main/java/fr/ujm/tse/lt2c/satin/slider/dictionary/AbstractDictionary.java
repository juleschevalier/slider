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

import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

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
    public static long disjointWith = 0;
    public static long distinctMembers = 0;
    public static long equivalentClass = 0;
    public static long equivalentProperty = 0;
    public static long FunctionalProperty = 0;
    public static long hasKey = 0;
    public static long hasValue = 0;
    public static long intersectionOf = 0;
    public static long InverseFunctionalProperty = 0;
    public static long inverseOf = 0;
    public static long IrreflexiveProperty = 0;
    public static long maxCardinality = 0;
    public static long maxQualifiedCardinality = 0;
    public static long member = 0;
    public static long Nothing = 0;
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
    public static long Resource = 0;
    public static long Property = 0;
    public static long ContainerMembershipProperty = 0;
    public static long Literal = 0;
    public static long Datatype = 0;

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
        domain = this.add(RDFS.domain.asNode());
        range = this.add(RDFS.range.asNode());
        type = this.add(RDF.type.asNode());
        subClassOf = this.add(RDFS.subClassOf.asNode());
        subPropertyOf = this.add(RDFS.subPropertyOf.asNode());
        equivalentClass = this.add(OWL.equivalentClass.asNode());
        equivalentProperty = this.add(OWL.equivalentProperty.asNode());
        sameAs = this.add(OWL.sameAs.asNode());
        inverseOf = this.add(OWL.inverseOf.asNode());
        propertyDisjointWith = this.add(OWL2.propertyDisjointWith.asNode());
        differentFrom = this.add(OWL.differentFrom.asNode());
        allDifferent = this.add(OWL.AllDifferent.asNode());
        allDisjointClasses = this.add(OWL2.AllDisjointClasses.asNode());
        allValuesFrom = this.add(OWL.allValuesFrom.asNode());
        annotationProperty = this.add(OWL.AnnotationProperty.asNode());
        assertionProperty = this.add(OWL2.assertionProperty.asNode());
        asymetricProperty = this.add(OWL2.AsymmetricProperty.asNode());
        classOwl = this.add(OWL.Class.asNode());
        classRdfs = this.add(RDFS.Class.asNode());
        complementOf = this.add(OWL.complementOf.asNode());
        disjointWith = this.add(OWL2.disjointWith.asNode());
        distinctMembers = this.add(OWL2.distinctMembers.asNode());
        FunctionalProperty = this.add(OWL.FunctionalProperty.asNode());
        hasKey = this.add(OWL2.hasKey.asNode());
        hasValue = this.add(OWL.hasValue.asNode());
        intersectionOf = this.add(OWL.intersectionOf.asNode());
        InverseFunctionalProperty = this.add(OWL.InverseFunctionalProperty.asNode());
        IrreflexiveProperty = this.add(OWL2.IrreflexiveProperty.asNode());
        maxCardinality = this.add(OWL.maxCardinality.asNode());
        maxQualifiedCardinality = this.add(OWL2.maxQualifiedCardinality.asNode());
        member = this.add(RDFS.member.asNode());
        Nothing = this.add(OWL.Nothing.asNode());
        onClass = this.add(OWL2.onClass.asNode());
        onProperty = this.add(OWL.onProperty.asNode());
        oneOf = this.add(OWL.oneOf.asNode());
        propertyChainAxiom = this.add(OWL2.propertyChainAxiom.asNode());
        someValuesFrom = this.add(OWL.someValuesFrom.asNode());
        sourceIndividual = this.add(OWL2.sourceIndividual.asNode());
        symetricProperty = this.add(OWL2.SymmetricProperty.asNode());
        targetIndividual = this.add(OWL2.targetIndividual.asNode());
        targetValue = this.add(OWL2.targetValue.asNode());
        thing = this.add(OWL.Thing.asNode());
        transitiveProperty = this.add(OWL.TransitiveProperty.asNode());
        unionOf = this.add(OWL.unionOf.asNode());
        Resource = this.add(RDFS.Resource.asNode());
        Property = this.add(RDF.Property.asNode());
        ContainerMembershipProperty = this.add(RDFS.ContainerMembershipProperty.asNode());
        Literal = this.add(RDFS.Literal.asNode());
        Datatype = this.add(RDFS.Datatype.asNode());
    }
}
