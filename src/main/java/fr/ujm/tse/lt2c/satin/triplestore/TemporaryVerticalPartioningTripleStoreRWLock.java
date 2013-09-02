package fr.ujm.tse.lt2c.satin.triplestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;

/**
 * 
 * @author Julien Subercaze
 * 
 *         Triple Store that implements vertical partioning approach
 */
public class TemporaryVerticalPartioningTripleStoreRWLock implements TripleStore {

	HashMap<Long, Multimap<Long, Long>> internalstore;
	ArrayList<Triple> triplesCollection;
	ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
	int triples;

	public TemporaryVerticalPartioningTripleStoreRWLock() {
		internalstore = new HashMap<>();
		triplesCollection = new ArrayList<>();
		triples = 0;
	}

	@Override
	public void add(Triple t) {
		rwlock.writeLock().lock();
		try {
			if (!internalstore.containsKey(t.getPredicate())) {
				Multimap<Long, Long> newmap = HashMultimap.create();
				newmap.put(t.getSubject(), t.getObject());
				internalstore.put(t.getPredicate(), newmap);
				triples++;

			} else {
				if (internalstore.get(t.getPredicate()).put(t.getSubject(),
						t.getObject()))
					triples++;
			}
			triplesCollection.add(t);
		} catch (Exception e) {

		} finally {
			rwlock.writeLock().unlock();
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
		return triplesCollection;
	}

	@Override
	public Collection<Triple> getbySubject(long s) {
		Collection<Triple> result = new ArrayList<>(triples);
		rwlock.readLock().lock();
		try {
			for (Long predicate : internalstore.keySet()) {
				Multimap<Long, Long> multimap = internalstore.get(predicate);
				if (multimap == null)
					continue;
				for (Entry<Long, Long> entry : multimap.entries()) {
					if (entry.getKey() == s)
						result.add(new TripleImplNaive(entry.getKey(),
								predicate, entry.getValue()));
				}
			}
		} catch (Exception e) {

		} finally {
			rwlock.readLock().unlock();
		}
		return result;
	}

	@Override
	public Collection<Triple> getbyPredicate(long p) {
		Collection<Triple> result = new ArrayList<>(triples);
		rwlock.readLock().lock();
		try {
			Multimap<Long, Long> multimap = internalstore.get(p);
			if (multimap != null) {
				for (Entry<Long, Long> entry : multimap.entries()) {
					result.add(new TripleImplNaive(entry.getKey(), p, entry
							.getValue()));
				}
			}
		} catch (Exception e) {

		} finally {
			rwlock.readLock().unlock();
		}
		return result;
	}

	@Override
	public Collection<Triple> getbyObject(long o) {
		Collection<Triple> result = new ArrayList<>(triples);
		rwlock.readLock().lock();
		try {
			for (Long predicate : internalstore.keySet()) {
				Multimap<Long, Long> multimap = internalstore.get(predicate);
				if (multimap == null)
					continue;
				for (Entry<Long, Long> entry : multimap.entries()) {
					if (entry.getValue() == o)
						result.add(new TripleImplNaive(entry.getKey(),
								predicate, entry.getValue()));
				}
			}
		} catch (Exception e) {

		} finally {
			rwlock.readLock().unlock();
		}
		return result;
	}

	@Override
	public long size() {
		return triples;
	}

	@Override
	public boolean isEmpty() {
		return triples == 0;
	}

	/**
	 * Unimplemented
	 */
	@Override
	public void writeToFile(String file, Dictionnary dictionnary) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean contains(Triple triple) {
		if (!internalstore.containsKey(triple.getPredicate()))
			return false;
		return (internalstore.get(triple.getPredicate()).containsEntry(
				triple.getSubject(), triple.getObject()));

	}
	
	public void clear() {
		this.internalstore.clear();
		this.triples = 0;
		this.triplesCollection.clear();
		
	}
}
