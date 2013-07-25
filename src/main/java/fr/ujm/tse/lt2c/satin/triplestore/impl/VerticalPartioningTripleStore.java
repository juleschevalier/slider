package fr.ujm.tse.lt2c.satin.triplestore.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleImplNaive;

/**
 * 
 * @author Julien Subercaze
 * 
 *         Triple Store that implements vertical partioning approach
 */
public class VerticalPartioningTripleStore implements TripleStore {

	HashMap<Long, Multimap<Long, Long>> internalstore;
	int triples;

	public VerticalPartioningTripleStore() {
		internalstore = new HashMap<>();
		triples = 0;
	}

	@Override
	public void add(Triple t) {
		if (!internalstore.containsKey(t.getPredicate())) {
			Multimap<Long, Long> newmap = HashMultimap.create();
			if (newmap.put(t.getSubject(), t.getObject())) {
				triples++;
			}

		} else {
			if (internalstore.get(t.getPredicate()).put(t.getSubject(),
					t.getObject()))
				triples++;
		}

	}

	@Override
	public void addAll(Collection<Triple> t) {
		for (Triple triple : t) {
			add(triple);
		}

	}

	@Override
	public Collection<Triple> getAll() {
		Collection<Triple> result = new ArrayList<>(triples);
		for (Long predicate : internalstore.keySet()) {
			Multimap<Long, Long> multimap = internalstore.get(predicate);
			for (Entry<Long, Long> entry : multimap.entries()) {
				result.add(new TripleImplNaive(entry.getKey(), predicate, entry
						.getValue()));
			}
		}
		return result;
	}

	@Override
	public Collection<Triple> getbySubject(long s) {
		Collection<Triple> result = new ArrayList<>(triples);
		for (Long predicate : internalstore.keySet()) {
			Multimap<Long, Long> multimap = internalstore.get(predicate);
			for (Entry<Long, Long> entry : multimap.entries()) {
				if (entry.getKey() == s)
					result.add(new TripleImplNaive(entry.getKey(), predicate,
							entry.getValue()));
			}
		}
		return result;
	}

	@Override
	public Collection<Triple> getbyPredicate(long p) {
		Multimap<Long, Long> multimap = internalstore.get(p);
		Collection<Triple> result = new ArrayList<>(triples);
		for (Entry<Long, Long> entry : multimap.entries()) {
			result.add(new TripleImplNaive(entry.getKey(), p, entry.getValue()));
		}
		return result;
	}

	@Override
	public Collection<Triple> getbyObject(long o) {
		Collection<Triple> result = new ArrayList<>(triples);
		for (Long predicate : internalstore.keySet()) {
			Multimap<Long, Long> multimap = internalstore.get(predicate);
			for (Entry<Long, Long> entry : multimap.entries()) {
				if (entry.getValue() == o)
					result.add(new TripleImplNaive(entry.getKey(), predicate,
							entry.getValue()));
			}
		}
		return result;
	}
	/**
	 * Unimplemented
	 */
	@Override
	public void writeToFile(String file, Dictionnary dictionnary) {
		// TODO Auto-generated method stub

	}

}
