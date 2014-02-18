package fr.ujm.tse.lt2c.satin.triplestore;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;

/**
 * Triple Store that implements vertical partioning approach
 * 
 * @author Julien Subercaze
 * @see Triple
 */
public class VerticalPartioningTripleStoreRWLock implements TripleStore {

    private static Logger logger = Logger.getLogger(VerticalPartioningTripleStoreRWLock.class);
    Map<Long, Multimap<Long, Long>> internalstore;
    ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
    int triples;

    /**
     * Constructor
     */
    public VerticalPartioningTripleStoreRWLock() {
        this.internalstore = new HashMap<>();
        this.triples = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.ujm.tse.lt2c.satin.interfaces.TripleStore#add(fr.ujm.tse.lt2c.satin
     * .interfaces.Triple)
     */
    @Override
    public void add(final Triple t) {
        this.rwlock.writeLock().lock();
        try {
            if (!this.internalstore.containsKey(t.getPredicate())) {
                final Multimap<Long, Long> newmap = HashMultimap.create();
                newmap.put(t.getSubject(), t.getObject());
                this.internalstore.put(t.getPredicate(), newmap);
                this.triples++;

            } else {
                if (!(this.internalstore.get(t.getPredicate()).containsEntry(t.getSubject(), t.getObject())) && (this.internalstore.get(t.getPredicate()).put(t.getSubject(), t.getObject()))) {
                    this.triples++;
                }
            }
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.rwlock.writeLock().unlock();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.ujm.tse.lt2c.satin.interfaces.TripleStore#addAll(java.util.Collection)
     */
    @Override
    public void addAll(final Collection<Triple> t) {
        for (final Triple triple : t) {
            this.add(triple);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ujm.tse.lt2c.satin.interfaces.TripleStore#getAll()
     */
    @Override
    public Collection<Triple> getAll() {
        this.rwlock.readLock().lock();
        final Collection<Triple> result = new ArrayList<>(this.triples);
        try {
            for (final Long predicate : this.internalstore.keySet()) {
                final Multimap<Long, Long> multimap = this.internalstore.get(predicate);
                for (final Entry<Long, Long> entry : multimap.entries()) {
                    result.add(new ImmutableTriple(entry.getKey(), predicate, entry.getValue()));
                }
            }
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.rwlock.readLock().unlock();
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ujm.tse.lt2c.satin.interfaces.TripleStore#getbySubject(long)
     */
    @Override
    public Collection<Triple> getbySubject(final long s) {
        this.rwlock.readLock().lock();
        final Collection<Triple> result = new ArrayList<>(this.triples);
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
        } finally {
            this.rwlock.readLock().unlock();
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ujm.tse.lt2c.satin.interfaces.TripleStore#getbyPredicate(long)
     */
    @Override
    public Collection<Triple> getbyPredicate(final long p) {
        this.rwlock.readLock().lock();
        final Collection<Triple> result = new ArrayList<>(this.triples);
        try {
            final Multimap<Long, Long> multimap = this.internalstore.get(p);
            if (multimap != null) {
                for (final Entry<Long, Long> entry : multimap.entries()) {
                    result.add(new ImmutableTriple(entry.getKey(), p, entry.getValue()));
                }
            }
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.rwlock.readLock().unlock();
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ujm.tse.lt2c.satin.interfaces.TripleStore#getbyObject(long)
     */
    @Override
    public Collection<Triple> getbyObject(final long o) {
        this.rwlock.readLock().lock();
        final Collection<Triple> result = new ArrayList<>(this.triples);
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
            this.rwlock.readLock().unlock();
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ujm.tse.lt2c.satin.interfaces.TripleStore#size()
     */
    @Override
    public long size() {
        this.rwlock.readLock().lock();
        long result = 0;
        try {
            result = this.triples;

        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.rwlock.readLock().unlock();
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ujm.tse.lt2c.satin.interfaces.TripleStore#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        this.rwlock.readLock().lock();
        boolean result = false;
        try {
            result = this.triples == 0;

        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.rwlock.readLock().unlock();
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.ujm.tse.lt2c.satin.interfaces.TripleStore#writeToFile(java.lang.String
     * , fr.ujm.tse.lt2c.satin.interfaces.Dictionary)
     */
    @Override
    public void writeToFile(final String file, final Dictionary dictionary) {
        // try {
        // // Create file
        // final FileWriter fstream = new FileWriter(file, false);
        // final BufferedWriter out = new BufferedWriter(fstream);
        // for (final Triple triple : this.getAll()) {
        // out.write(dictionary.printTriple(triple) + "\n");
        // }
        // // Close the output stream
        // out.close();
        // } catch (final Exception e) {
        // // Catch exception if any
        // logger.error("", e);
        // }
        // Create an empty model.
        final Model model = ModelFactory.createDefaultModel();
        // Add all the triples into the model
        for (final Triple triple : this.getAll()) {
            final Resource subject = ResourceFactory.createResource(dictionary.get(triple.getSubject()));
            final Property predicate = ResourceFactory.createProperty(dictionary.get(triple.getPredicate()));
            final Resource object = ResourceFactory.createResource(dictionary.get(triple.getObject()));
            model.add(subject, predicate, object);
        }
        try {
            final OutputStream os = new FileOutputStream(file);
            model.write(os, "N-TRIPLES");
            os.close();
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.ujm.tse.lt2c.satin.interfaces.TripleStore#contains(fr.ujm.tse.lt2c
     * .satin.interfaces.Triple)
     */
    @Override
    public boolean contains(final Triple triple) {
        this.rwlock.readLock().lock();
        boolean result = false;
        try {
            if (this.internalstore.containsKey(triple.getPredicate())) {
                result = (this.internalstore.get(triple.getPredicate()).containsEntry(triple.getSubject(), triple.getObject()));
            }

        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.rwlock.readLock().unlock();
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.ujm.tse.lt2c.satin.interfaces.TripleStore#contains(fr.ujm.tse.lt2c
     * .satin.interfaces.Triple)
     */
    @Override
    public boolean contains(final long s, final long p, final long o) {
        this.rwlock.readLock().lock();
        boolean result = false;
        try {
            if (this.internalstore.containsKey(p)) {
                result = (this.internalstore.get(p).containsEntry(s, o));
            }

        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.rwlock.readLock().unlock();
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.ujm.tse.lt2c.satin.interfaces.TripleStore#getMultiMapForPredicate(
     * long)
     */
    @Override
    public Multimap<Long, Long> getMultiMapForPredicate(final long p) {
        this.rwlock.readLock().lock();
        Multimap<Long, Long> multimap = null;
        try {
            if (this.internalstore.get(p) != null) {
                multimap = ImmutableMultimap.copyOf(this.internalstore.get(p));
            }
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.rwlock.readLock().unlock();
        }
        return multimap;
    }

    @Override
    public Collection<Long> getPredicates() {

        return this.internalstore.keySet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ujm.tse.lt2c.satin.interfaces.TripleStore#clear()
     */
    @Override
    public void clear() {
        this.rwlock.readLock().lock();
        try {
            this.internalstore.clear();
            this.triples = 0;
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.rwlock.readLock().unlock();
        }

    }

}
