package fr.ujm.tse.lt2c.satin.triplestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;

/**
 * 
 * @author Julien Subercaze
 * 
 *         Triple Store that implements vertical partioning approach
 */
public class TemporaryVerticalPartioningTripleStoreRWLock implements
TripleStore {
	private static Logger logger = Logger.getLogger(TemporaryVerticalPartioningTripleStoreRWLock.class);
	HashMap<Long, Multimap<Long, Long>> internalstore;
	Set<Triple> triplesCollection;
	ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
	int triples;

	public TemporaryVerticalPartioningTripleStoreRWLock() {
		internalstore = new HashMap<>();
		triplesCollection = new HashSet<>();
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
				if (!(internalstore.get(t.getPredicate()).containsEntry(
						t.getSubject(), t.getObject()))) {
					if (internalstore.get(t.getPredicate()).put(t.getSubject(),
							t.getObject()))
						triples++;
				}
			}
			triplesCollection.add(t);
		} catch (Exception e) {
			logger.debug(e.getMessage());
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
		Collection<Triple> copy = null;
		rwlock.readLock().lock();
		try {
			copy= ImmutableSet.copyOf(triplesCollection);//Avoid concurrent modification}
		}
		catch (Exception e) {
			logger.debug(e.getMessage());
		} finally {
			rwlock.readLock().unlock();
		}
		return copy;
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
			logger.debug(e.getMessage());
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
			logger.debug(e.getMessage());
		} finally {
			rwlock.readLock().unlock();
		}
		return result;
	}

	public Multimap<Long, Long> getMultiMapForPredicate(long p) {
		rwlock.readLock().lock();
		Multimap<Long, Long> multimap = null;
		try {
			multimap = ImmutableMultimap.copyOf(internalstore.get(p));
		} catch (Exception e) {
			logger.debug(e.getMessage());
		} finally {
			rwlock.readLock().unlock();
		}
		return multimap;
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
			logger.debug(e.getMessage());
		} finally {
			rwlock.readLock().unlock();
		}
		return result;
	}

	@Override
	public long size() {
		long result = 0;
		rwlock.readLock().lock();
		try {
			result = triples;
		} catch (Exception e) {
			logger.debug(e.getMessage());
		} finally {
			rwlock.readLock().unlock();
		}
		return result;
	}

	@Override
	public boolean isEmpty() {
		boolean result = false;
		rwlock.readLock().lock();
		try {
			result = triples == 0;

		} catch (Exception e) {
			logger.debug(e.getMessage());
		} finally {
			rwlock.readLock().unlock();
		}
		return result;
	}

	/**
	 * Unimplemented
	 */
	 @Override
	 public void writeToFile(String file, Dictionary dictionary) {
		 // TODO Auto-generated method stub

	 }

	 @Override
	 public boolean contains(Triple triple) {
		 boolean result = false;
		 rwlock.readLock().lock();
		 try {
			 if (!internalstore.containsKey(triple.getPredicate())) {

			 } else {
				 result = (internalstore.get(triple.getPredicate())
						 .containsEntry(triple.getSubject(), triple.getObject()));
			 }

		 } catch (Exception e) {
			 logger.debug(e.getMessage());
		 } finally {
			 rwlock.readLock().unlock();
		 }
		 return result;
	 }

	 public void clear() {
		 rwlock.writeLock().lock();
		 try {
			 this.internalstore.clear();
			 this.triples = 0;
			 this.triplesCollection.clear();
		 } catch (Exception e) {
			 logger.debug(e.getMessage());
		 } finally {
			 rwlock.writeLock().unlock();
		 }

	 }
}
