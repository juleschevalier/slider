package fr.ujm.tse.lt2c.satin.dictionary;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;


/**
 * @author Jules Chevalier
 *
 */
public abstract class AbstractDictionary implements Dictionary{

	public static long wedontcare = -1;
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


//	@Override
	public String printConcept(String c) {
		/*Replace integers, strings... by their value*/
		c=c.replaceAll("(\".*\")\\^\\^.*", "$1");
		
		/*Cut the url, keep the name*/
		if(c.split("#").length>1)
			c=c.split("#")[1];

		/*Replace all blanknodes by BLANKNODE*/
		c=c.replaceAll("-?[0-9a-z]+:[0-9a-z]+:-[0-9a-z]+", "BLANKNODE");
		
		return c;
	}
	
}
