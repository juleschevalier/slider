package fr.ujm.tse.lt2c.satin.slider.triplestore;

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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import fr.ujm.tse.lt2c.satin.slider.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.slider.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.slider.interfaces.TripleStore;

/**
 * Triple Store that implements vertical partioning approach
 * 
 * @author Julien Subercaze
 * @author Jules Chevalier
 * @see Triple
 */
public class VerticalPartioningTripleStoreRWSmartLock implements TripleStore {

    private static Logger logger = Logger.getLogger(VerticalPartioningTripleStoreRWSmartLock.class);

    private final Map<Long, Multimap<Long, Long>> internalstore;
    private final Map<Long, ReentrantReadWriteLock> predicatesLocks;
    private final ReentrantReadWriteLock globalLock;
    private final AtomicInteger triples;

    /**
     * Constructor
     */
    public VerticalPartioningTripleStoreRWSmartLock() {
        this.internalstore = new HashMap<Long, Multimap<Long, Long>>();
        this.predicatesLocks = new ConcurrentHashMap<Long, ReentrantReadWriteLock>();
        this.globalLock = new ReentrantReadWriteLock();
        this.triples = new AtomicInteger();
    }

    private ReentrantReadWriteLock getLock(final long p) {
        if (!this.predicatesLocks.containsKey(p)) {
            this.predicatesLocks.put(p, new ReentrantReadWriteLock());
        }
        return this.predicatesLocks.get(p);
    }

    @Override
    public boolean add(final Triple t) {
        /* Get predicate's multimap */
        boolean exists = false;
        this.globalLock.writeLock().lock();
        Multimap<Long, Long> map = null;
        try {
            if (!this.internalstore.containsKey(t.getPredicate())) {
                map = HashMultimap.create();
                this.internalstore.put(t.getPredicate(), map);
            } else {
                map = this.internalstore.get(t.getPredicate());
            }
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.globalLock.writeLock().unlock();
        }
        /* Add triple */
        final ReentrantReadWriteLock lock = this.getLock(t.getPredicate());
        lock.writeLock().lock();
        try {
            if (map.containsEntry(t.getSubject(), t.getObject()) || !map.put(t.getSubject(), t.getObject())) {
                exists = true;
            }
            if (!exists) {
                this.triples.incrementAndGet();
            }
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            lock.writeLock().unlock();
        }
        return exists;

    }

    @Override
    public Collection<Triple> addAll(final Collection<Triple> triples) {
        // TODO to improve
        final Collection<Triple> newTriples = new HashSet<>();
        for (final Triple triple : triples) {
            if (this.add(triple)) {
                newTriples.add(triple);
            }
        }
        return newTriples;

    }

    @Override
    public void remove(final Triple t) {
        /* Get predicate's multimap */
        this.globalLock.readLock().lock();
        Multimap<Long, Long> map = null;
        try {
            if (this.internalstore.containsKey(t.getPredicate())) {
                map = this.internalstore.get(t.getPredicate());
            }
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.globalLock.readLock().unlock();
            if (map == null) {
                return;
            }
        }
        /* Remove triple */
        final ReentrantReadWriteLock lock = this.getLock(t.getPredicate());
        lock.writeLock().lock();
        try {
            if (map.remove(t.getSubject(), t.getObject())) {
                this.triples.decrementAndGet();
            }
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            lock.writeLock().unlock();
        }

        /* Remove matching map if needed */
        this.globalLock.writeLock().lock();
        try {
            /* get the map again in case of concurrent add */
            map = this.internalstore.get(t.getPredicate());
            if (map.isEmpty()) {
                this.internalstore.remove(t.getPredicate());
            }
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.globalLock.writeLock().unlock();
            if (map == null) {
                return;
            }
        }
    }

    @Override
    public Collection<Triple> getAll() {
        this.globalLock.readLock().lock();
        final Collection<Triple> result = new ArrayList<Triple>(this.triples.get());
        try {
            for (final Long predicate : this.internalstore.keySet()) {

                final ReentrantReadWriteLock lock = this.getLock(predicate);
                lock.readLock().lock();
                try {
                    final Multimap<Long, Long> multimap = this.internalstore.get(predicate);
                    for (final Entry<Long, Long> entry : multimap.entries()) {
                        result.add(new ImmutableTriple(entry.getKey(), predicate, entry.getValue()));
                    }
                } catch (final Exception e) {
                    logger.error("", e);
                } finally {
                    lock.readLock().unlock();
                }
            }
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.globalLock.readLock().unlock();
        }
        return result;
    }

    @Override
    public Collection<Triple> getbySubject(final long s) {
        final Collection<Triple> result = new ArrayList<>(this.triples.get());
        try {
            for (final Long predicate : this.internalstore.keySet()) {
                final Multimap<Long, Long> multimap = this.internalstore.get(predicate);
                if (multimap == null) {
                    continue;
                }
                for (final Entry<Long, Long> entry : multimap.entries()) {
                    if (entry.getKey() == s) {
                        result.add(new ImmutableTriple(entry.getKey(), predicate, entry.getValue()));
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("", e);
        } finally {}
        return result;
    }

    @Override
    public Collection<Triple> getbyPredicate(final long p) {
        /* Get predicate's multimap */
        Collection<Triple> result = null;
        this.globalLock.readLock().lock();
        Multimap<Long, Long> map = null;
        try {
            map = this.internalstore.get(p);
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.globalLock.readLock().unlock();
            if (map == null) {
                return new ArrayList<Triple>();
            }
        }
        /* construct list of all triples */
        final ReentrantReadWriteLock lock = this.getLock(p);
        lock.writeLock().lock();
        try {
            result = new ArrayList<Triple>();
            for (final Entry<Long, Long> entry : map.entries()) {
                result.add(new ImmutableTriple(entry.getKey(), p, entry.getValue()));
            }
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            lock.writeLock().unlock();
        }
        return result;
    }

    @Override
    public Collection<Triple> getbyObject(final long o) {
        this.globalLock.readLock().lock();
        final Collection<Triple> result = new ArrayList<>(this.triples.get());
        try {
            for (final Long predicate : this.internalstore.keySet()) {
                final Multimap<Long, Long> multimap = this.internalstore.get(predicate);
                if (multimap == null) {
                    continue;
                }
                for (final Entry<Long, Long> entry : multimap.entries()) {
                    if (entry.getValue() == o) {
                        result.add(new ImmutableTriple(entry.getKey(), predicate, entry.getValue()));
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.globalLock.readLock().unlock();
        }
        return result;
    }

    @Override
    public long size() {
        return this.triples.get();
    }

    @Override
    public boolean isEmpty() {
        this.globalLock.readLock().lock();
        boolean result = false;
        try {
            result = this.triples.get() == 0;

        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.globalLock.readLock().unlock();
        }
        return result;
    }

    @Override
    public void writeToFile(final String file, final Dictionary dictionary) {
        // Create an empty model.
        final Model model = ModelFactory.createDefaultModel();
        // Add all the triples into the model
        for (final Triple triple : this.getAll()) {
            // final Resource subject = ResourceFactory.createResource(dictionary.get(triple.getSubject()));
            // final Property predicate = ResourceFactory.createProperty(dictionary.get(triple.getPredicate()));
            // final Resource object = ResourceFactory.createResource(dictionary.get(triple.getObject()));
            final Node subject = dictionary.get(triple.getSubject());
            final Node predicate = dictionary.get(triple.getPredicate());
            final Node object = dictionary.get(triple.getObject());
            final com.hp.hpl.jena.graph.Triple t = new com.hp.hpl.jena.graph.Triple(subject, predicate, object);
            final Statement st = model.asStatement(t);
            model.add(st);
        }
        try {
            final OutputStream os = new FileOutputStream(file);
            model.write(os, "N-TRIPLES");
            os.close();
        } catch (final FileNotFoundException e) {
            logger.error("", e);
        } catch (final IOException e) {
            logger.error("", e);
        }

    }

    @Override
    public boolean contains(final Triple triple) {
        this.globalLock.readLock().lock();
        boolean result = false;
        try {
            result = this.containsNoLock(triple.getSubject(), triple.getPredicate(), triple.getObject());

        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.globalLock.readLock().unlock();
        }
        return result;
    }

    @Override
    public boolean contains(final long s, final long p, final long o) {
        this.globalLock.readLock().lock();
        boolean result = false;
        try {
            result = this.containsNoLock(s, p, o);

        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.globalLock.readLock().unlock();
        }
        return result;
    }

    private boolean containsNoLock(final long s, final long p, final long o) {
        boolean result = false;
        if (this.internalstore.containsKey(p)) {
            result = this.internalstore.get(p).containsEntry(s, o);
        }
        return result;
    }

    @Override
    public Multimap<Long, Long> getMultiMapForPredicate(final long p) {
        this.globalLock.readLock().lock();
        Multimap<Long, Long> multimap = null;
        try {
            if (this.internalstore.get(p) != null) {
                multimap = ImmutableMultimap.copyOf(this.internalstore.get(p));
            }
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.globalLock.readLock().unlock();
        }
        return multimap;
    }

    @Override
    public Collection<Long> getPredicates() {

        return this.internalstore.keySet();
    }

    @Override
    public void clear() {
        this.globalLock.readLock().lock();
        try {
            this.internalstore.clear();
            this.triples.set(0);
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.globalLock.readLock().unlock();
        }

    }

    @Override
    public void add(final long s, final long p, final long o) {

        this.globalLock.writeLock().lock();
        try {
            if (!this.internalstore.containsKey(p)) {
                final Multimap<Long, Long> newmap = HashMultimap.create();
                newmap.put(s, o);
                this.internalstore.put(p, newmap);
                this.triples.incrementAndGet();

            } else {
                if (!this.internalstore.get(p).containsEntry(s, o) && this.internalstore.get(p).put(s, o)) {
                    this.triples.incrementAndGet();
                }
            }
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.globalLock.writeLock().unlock();
        }

    }

    @Override
    public int hashCode() {
        // TODO Hashcode is not implemented in atomic type
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.internalstore == null ? 0 : this.internalstore.hashCode());
        result = prime * result + (this.triples == null ? 0 : this.triples.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final VerticalPartioningTripleStoreRWSmartLock other = (VerticalPartioningTripleStoreRWSmartLock) obj;
        if (this.internalstore == null) {
            if (other.internalstore != null) {
                return false;
            }
        } else if (!this.internalstore.equals(other.internalstore)) {
            return false;
        }
        if (this.triples == null) {
            if (other.triples != null) {
                return false;
            }
        } else if (!this.triples.equals(other.triples)) {
            return false;
        }
        return true;
    }

}
