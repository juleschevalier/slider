package fr.ujm.tse.lt2c.satin.triplestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;

import com.google.common.collect.Multimap;


import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;

/**
 * 
 * @author Julien Subercaze
 * 
 *         Triple Store that implements vertical partioning approach
 */
public class TemporaryVerticalPartioningTripleStore extends VerticalPartioningTripleStore{
	
	ArrayList<Triple> triplesCollection;

	public TemporaryVerticalPartioningTripleStore() {
		super();
		triplesCollection = new ArrayList<>();
	}

	@Override
	public void add(Triple t) {
		super.add(t);
		this.triplesCollection.add(t);
	}

	@Override
	public void addAll(Collection<Triple> t) {
		for (Triple triple : t) {
			add(triple);
		}

	}

	@Override
	public Collection<Triple> getAll() {
		return this.triplesCollection;
	}

	@Override
	public Collection<Triple> getbySubject(long s) {
		Collection<Triple> result = new ArrayList<>(triples);
		for (Long predicate : internalstore.keySet()) {
			Multimap<Long, Long> multimap = internalstore.get(predicate);
			if(multimap==null)
				continue;
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
		if(multimap!=null){
			for (Entry<Long, Long> entry : multimap.entries()) {
				result.add(new TripleImplNaive(entry.getKey(), p, entry.getValue()));
			}
		}
		return result;
	}

	@Override
	public Collection<Triple> getbyObject(long o) {
		Collection<Triple> result = new ArrayList<>(triples);
		for (Long predicate : internalstore.keySet()) {
			Multimap<Long, Long> multimap = internalstore.get(predicate);
			if(multimap==null)
				continue;
			for (Entry<Long, Long> entry : multimap.entries()) {
				if (entry.getValue() == o)
					result.add(new TripleImplNaive(entry.getKey(), predicate,
							entry.getValue()));
			}
		}
		return result;
	}

	@Override
	public long size(){
		return triples;
	}

	/**
	 * Unimplemented
	 */
	@Override
	public void writeToFile(String file, Dictionnary dictionnary) {
		// TODO Auto-generated method stub

	}

	public void clear() {
		this.internalstore.clear();
		this.triples = 0;
		this.triplesCollection.clear();
		
	}

}
