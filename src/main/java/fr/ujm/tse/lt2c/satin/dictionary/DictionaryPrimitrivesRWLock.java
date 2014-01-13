package fr.ujm.tse.lt2c.satin.dictionary;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import fr.ujm.tse.lt2c.satin.interfaces.Triple;

/**
 * @author Jules Chevalier
 * 
 */
public class DictionaryPrimitrivesRWLock extends AbstractDictionary {

	private Map<String, Long> triples = new HashMap<>();
	long counter;
	long primitivesCounter;

	ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();

	public DictionaryPrimitrivesRWLock() {
		super();
		this.triples = new HashMap<>();
		this.counter = 0;
		this.primitivesCounter = -1;
		wedontcare = add("X");
		// RhoDF
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
	public final long add(String s) {
		rwlock.writeLock().lock();
		if (this.triples.containsKey(s)) {
			long id = this.get(s);
			rwlock.writeLock().unlock();
			return id;
		}
		long id;
		if (s.matches("(\".*\")\\^\\^.*")) {
			this.triples.put(s, this.primitivesCounter);
			id = this.primitivesCounter--;
		} else {
			this.triples.put(s, this.counter);
			id = this.counter++;
		}
		rwlock.writeLock().unlock();
		return id;
	}

	@Override
	public final String get(long index) {
		rwlock.readLock().lock();
		Iterator<Entry<String, Long>> it = this.triples.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Long> pairs = (Entry<String, Long>) it.next();
			if (pairs.getValue().equals(index)) {
				String value = pairs.getKey();
				rwlock.readLock().unlock();
				return value;
			}
		}
		rwlock.readLock().unlock();
		return null;
	}

	@Override
	public final long get(String s) {
		rwlock.readLock().lock();
		long id = this.triples.get(s);
		rwlock.readLock().unlock();
		return id;
	}

	@Override
	public final long size() {
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
		DictionaryPrimitrivesRWLock other = (DictionaryPrimitrivesRWLock) obj;
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
	public String printTriple(Triple t) {
		rwlock.readLock().lock();
		String s = printConcept(this.get(t.getSubject())), p = printConcept(this.get(t.getPredicate())), o = printConcept(this.get(t.getObject()));
		rwlock.readLock().unlock();

		return s + " " + p + " " + o;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		rwlock.readLock().lock();
		for (String s : triples.keySet()) {
			sb.append(s + " ==> " + printConcept(s) + "\n");
		}
		rwlock.readLock().unlock();
		return sb.toString();
	}

}
