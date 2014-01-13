package fr.ujm.tse.lt2c.satin.triplestore;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
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
public class VerticalPartioningTripleStoreRWLock implements TripleStore {
	private static Logger logger = Logger.getLogger(VerticalPartioningTripleStoreRWLock.class);
	Map<Long, Multimap<Long, Long>> internalstore;
	ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
	int triples;

	public VerticalPartioningTripleStoreRWLock() {
		internalstore = new HashMap<>();
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
				if (!(internalstore.get(t.getPredicate()).containsEntry(t.getSubject(), t.getObject()))) {
					if (internalstore.get(t.getPredicate()).put(t.getSubject(), t.getObject()))
						triples++;
				}
			}
		} catch (Exception e) {
			logger.error("",e);
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
		rwlock.readLock().lock();
		Collection<Triple> result = new ArrayList<>(triples);
		try {
			for (Long predicate : internalstore.keySet()) {
				Multimap<Long, Long> multimap = internalstore.get(predicate);
				for (Entry<Long, Long> entry : multimap.entries()) {
					result.add(new ImmutableTriple(entry.getKey(), predicate, entry.getValue()));
				}
			}
		} catch (Exception e) {
			logger.error("",e);
		} finally {
			rwlock.readLock().unlock();
		}
		return result;
	}

	@Override
	public Collection<Triple> getbySubject(long s) {
		rwlock.readLock().lock();
		Collection<Triple> result = new ArrayList<>(triples);
		try {
			for (Long predicate : internalstore.keySet()) {
				Multimap<Long, Long> multimap = internalstore.get(predicate);
				if (multimap == null)
					continue;
				for (Entry<Long, Long> entry : multimap.entries()) {
					if (entry.getKey() == s)
						result.add(new ImmutableTriple(entry.getKey(), predicate, entry.getValue()));
				}
			}
		} catch (Exception e) {
			logger.error("",e);
		} finally {
			rwlock.readLock().unlock();
		}
		return result;
	}

	@Override
	public Collection<Triple> getbyPredicate(long p) {
		rwlock.readLock().lock();
		Collection<Triple> result = new ArrayList<>(triples);
		try {
			Multimap<Long, Long> multimap = internalstore.get(p);
			if (multimap != null) {
				for (Entry<Long, Long> entry : multimap.entries()) {
					result.add(new ImmutableTriple(entry.getKey(), p, entry.getValue()));
				}
			}
		} catch (Exception e) {
			logger.error("",e);
		} finally {
			rwlock.readLock().unlock();
		}
		return result;
	}

	@Override
	public Collection<Triple> getbyObject(long o) {
		rwlock.readLock().lock();
		Collection<Triple> result = new ArrayList<>(triples);
		try {
			for (Long predicate : internalstore.keySet()) {
				Multimap<Long, Long> multimap = internalstore.get(predicate);
				if (multimap == null)
					continue;
				for (Entry<Long, Long> entry : multimap.entries()) {
					if (entry.getValue() == o)
						result.add(new ImmutableTriple(entry.getKey(), predicate, entry.getValue()));
				}
			}
		} catch (Exception e) {
			logger.error("",e);
		} finally {
			rwlock.readLock().unlock();
		}
		return result;
	}

	@Override
	public long size() {
		rwlock.readLock().lock();
		long result = 0;
		try {
			result = triples;

		} catch (Exception e) {
			logger.error("",e);
		} finally {
			rwlock.readLock().unlock();
		}
		return result;
	}

	@Override
	public boolean isEmpty() {
		rwlock.readLock().lock();
		boolean result = false;
		try {
			result = triples == 0;

		} catch (Exception e) {
			logger.error("",e);
		} finally {
			rwlock.readLock().unlock();
		}
		return result;
	}

	@Override
	public void writeToFile(String file, Dictionary dictionary) {
		try {
			// Create file
			FileWriter fstream = new FileWriter(file, false);
			BufferedWriter out = new BufferedWriter(fstream);
			for (Triple triple : this.getAll()) {
				out.write(dictionary.printTriple(triple) + "\n");
			}
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			logger.error("",e);
		}

	}

	@Override
	public boolean contains(Triple triple) {
		rwlock.readLock().lock();
		boolean result = false;
		try {
			if (!internalstore.containsKey(triple.getPredicate())) {

			} else {
				result = (internalstore.get(triple.getPredicate()).containsEntry(triple.getSubject(), triple.getObject()));
			}

		} catch (Exception e) {
			logger.error("",e);
		} finally {
			rwlock.readLock().unlock();
		}
		return result;
	}

	@Override
	public Multimap<Long, Long> getMultiMapForPredicate(long p) {
		rwlock.readLock().lock();
		Multimap<Long, Long> multimap = null;
		try {
			if (internalstore.get(p) != null)
				multimap = ImmutableMultimap.copyOf(internalstore.get(p));
		} catch (Exception e) {
			logger.error("",e);
		} finally {
			rwlock.readLock().unlock();
		}
		return multimap;
	}

	@Override
	public void clear() {
		rwlock.readLock().lock();
		try {
			internalstore.clear();
			triples = 0;
		} catch (Exception e) {
			logger.error("",e);
		} finally {
			rwlock.readLock().unlock();
		}

	}
}
