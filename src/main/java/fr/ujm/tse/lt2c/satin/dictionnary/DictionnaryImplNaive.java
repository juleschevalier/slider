package fr.ujm.tse.lt2c.satin.dictionnary;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import fr.ujm.tse.lt2c.satin.interfaces.Triple;


/**
 * @author Jules Chevalier
 *
 */
public class DictionnaryImplNaive extends AbstractDictionnary{

	private HashMap<String,Long> triples= new HashMap<>();
	long counter;

	
	public DictionnaryImplNaive() {
		super();
		this.triples = new HashMap<>();
		this.counter = 0;
		//RhoDF
		domain = add("http://www.w3.org/2000/01/rdf-schema#domain");
		range = add("http://www.w3.org/2000/01/rdf-schema#range");
		type = add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		subClassOf = add("http://www.w3.org/2000/01/rdf-schema#subClassOf");
		subPropertyOf = add("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");
		equivalentClass = add("http://www.w3.org/2002/07/owl#equivalentClass");
		equivalentProperty = add("http://www.w3.org/2002/07/owl#equivalentProperty");
		sameAs = add("http://www.w3.org/2002/07/owl#sameAs");
		
		inverseOf = add("http://www.w3.org/2002/07/owl#inverseOf");
		propertyDisjointWith = add("");
		differentFrom = add("");
		allDifferent = add("http://www.w3.org/2002/07/owl#AllDifferent");
		allDisjointClasses = add("");
		allValuesFrom = add("http://www.w3.org/2002/07/owl#allValuesFrom");
		annotationProperty = add("http://www.w3.org/2002/07/owl#AnnotationProperty");
		assertionProperty = add("");
		asymetricProperty = add("");
		clazz = add("http://www.w3.org/2002/07/owl#Class");
		complementOf = add("");
		disjoinWith = add("http://www.w3.org/2002/07/owl#disjointWith");
		distinctmembers = add("http://www.w3.org/2002/07/owl#distinctMembers");
		functionalProperty = add("");
		hasKey = add("");
		hasValue = add("");
		intersectionOf = add("");
		inverseFunctionalProperty = add("");
		irreflexiveProperty = add("");
		maxCardinality = add("http://www.w3.org/2002/07/owl#maxCardinality");
		maxQualifiedCardinality = add("");
		members = add("");
		nothing = add("");
		onClass = add("");
		onProperty = add("http://www.w3.org/2002/07/owl#onProperty");
		oneOf = add("");
		propertyChainAxiom = add("");
		someValuesFrom = add("");
		sourceIndividual = add("");
		symetricProperty = add("");
		targetIndividual = add("");
		targetValue = add("");
		thing = add("http://www.w3.org/2002/07/owl#Thing");
		transitiveProperty = add("http://www.w3.org/2002/07/owl#TransitiveProperty");
		unionOf = add("");
	}

	@Override
	public synchronized long add(String s) {
		if(this.triples.containsKey(s)){
			return this.get(s);
		}
		this.triples.put(s, this.counter);
		return this.counter++;
	}

	@Override
	public synchronized String get(long index) {
		Iterator<Entry<String, Long>> it = this.triples.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Long> pairs = (Entry<String, Long>) it.next();
			if (pairs.getValue().equals(index))
				return pairs.getKey();
		}
		return null;
	}

	@Override
	public synchronized long get(String s) {
		return this.triples.get(s);
	}

	@Override
	public long size() {
		return this.triples.size();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (counter ^ (counter >>> 32));
		result = prime * result + ((triples == null) ? 0 : triples.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DictionnaryImplNaive other = (DictionnaryImplNaive) obj;
		if (counter != other.counter)
			return false;
		if (triples == null) {
			if (other.triples != null)
				return false;
		} else if (!triples.equals(other.triples))
			return false;
		return true;
	}
	
	@Override
	public String printTriple(Triple t){
		String s = this.get(t.getSubject()),
			   p = this.get(t.getPredicate()),
			   o = this.get(t.getObject());
		
		if(s.split("#").length>1)
			s=s.split("#")[1];
		if(p.split("#").length>1)
			p=p.split("#")[1];
		if(o.split("#").length>1)
			o=o.split("#")[1];
		
		return s+" "+p+" "+o;
	}

}
