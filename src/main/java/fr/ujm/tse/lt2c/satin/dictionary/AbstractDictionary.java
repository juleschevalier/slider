package fr.ujm.tse.lt2c.satin.dictionary;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;

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
	public static long clazz = 0;
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
	public static long members = 0;
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

	@Override
	public String printConcept(String c) {
		/* Replace integers, strings... by their value */
		c = c.replaceAll("(\".*\")\\^\\^.*", "$1");

		/* Cut the URL, keep the name */
		if (c.split("#").length > 1) {
			c = c.split("#")[1];
		}

		/* Replace all the blank nodes by "BLANKNODE" */
		c = c.replaceAll("-?[0-9a-z]+:[0-9a-z]+:-[0-9a-z]+", "BLANKNODE");

		return c;
	}

	public void initialize() {
		/*
		 * All the prefixes come from prefix.cc
		 */
		domain = add("http://www.w3.org/2000/01/rdf-schema#domain");
		range = add("http://www.w3.org/2000/01/rdf-schema#range");
		type = add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		subClassOf = add("http://www.w3.org/2000/01/rdf-schema#subClassOf");
		subPropertyOf = add("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");
		equivalentClass = add("http://www.w3.org/2002/07/owl#equivalentClass");
		equivalentProperty = add("http://www.w3.org/2002/07/owl#equivalentProperty");
		sameAs = add("http://www.w3.org/2002/07/owl#sameAs");
		inverseOf = add("http://www.w3.org/2002/07/owl#inverseOf");
		propertyDisjointWith = add("http://www.w3.org/2002/07/owl#propertyDisjointWith");
		differentFrom = add("http://www.w3.org/2002/07/owl#differentFrom");
		allDifferent = add("http://www.w3.org/2002/07/owl#AllDifferent");
		allDisjointClasses = add("http://www.w3.org/2002/07/owl#allDisjointClasses");
		allValuesFrom = add("http://www.w3.org/2002/07/owl#allValuesFrom");
		annotationProperty = add("http://www.w3.org/2002/07/owl#AnnotationProperty");
		assertionProperty = add("http://www.w3.org/2002/07/owl#assertionProperty");
		asymetricProperty = add("http://www.w3.org/2002/07/owl#asymetricProperty");
		clazz = add("http://www.w3.org/2002/07/owl#Class");
		complementOf = add("http://www.w3.org/2002/07/owl#complementOf");
		disjoinWith = add("http://www.w3.org/2002/07/owl#disjointWith");
		distinctmembers = add("http://www.w3.org/2002/07/owl#distinctMembers");
		functionalProperty = add("http://www.w3.org/2002/07/owl#functionalProperty");
		hasKey = add("http://www.w3.org/2002/07/owl#hasKey");
		hasValue = add("http://www.w3.org/2002/07/owl#hasValue");
		intersectionOf = add("http://www.w3.org/2002/07/owl#intersectionOf");
		inverseFunctionalProperty = add("http://www.w3.org/2002/07/owl#inverseFunctionalProperty");
		irreflexiveProperty = add("http://www.w3.org/2002/07/owl#irreflexiveProperty");
		maxCardinality = add("http://www.w3.org/2002/07/owl#maxCardinality");
		maxQualifiedCardinality = add("http://www.w3.org/2002/07/owl#maxQualifiedCardinality");
		members = add("http://www.w3.org/2002/07/owl#members");
		nothing = add("http://www.w3.org/2002/07/owl#nothing");
		onClass = add("http://www.w3.org/2002/07/owl#onClass");
		onProperty = add("http://www.w3.org/2002/07/owl#onProperty");
		oneOf = add("http://www.w3.org/2002/07/owl#oneOf");
		propertyChainAxiom = add("http://www.w3.org/2002/07/owl#propertyChainAxiom");
		someValuesFrom = add("http://www.w3.org/2002/07/owl#someValuesFrom");
		sourceIndividual = add("http://www.w3.org/2002/07/owl#sourceIndividual");
		symetricProperty = add("http://www.w3.org/2002/07/owl#symetricProperty");
		targetIndividual = add("http://www.w3.org/2002/07/owl#targetIndividual");
		targetValue = add("http://www.w3.org/2002/07/owl#targetValue");
		thing = add("http://www.w3.org/2002/07/owl#Thing");
		transitiveProperty = add("http://www.w3.org/2002/07/owl#TransitiveProperty");
		unionOf = add("http://www.w3.org/2002/07/owl#unionOf");
	}
}
