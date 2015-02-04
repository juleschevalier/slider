package fr.ujm.tse.lt2c.satin.slider.dictionary;

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

import fr.ujm.tse.lt2c.satin.slider.interfaces.Triple;

/**
 * Concurrent implementation of the {@link Dictionary}.
 * Literals and primitives are stores with negative id, concepts with positive ones
 * 
 * @author Jules Chevalier
 * 
 */
public class DictionaryPrimitrivesRWLock extends AbstractDictionary {
    private static final Logger LOGGER = Logger.getLogger(DictionaryPrimitrivesRWLock.class);

    private Map<Node, Long> triples = new HashMap<>();
    private long counter;
    private long primitivesCounter;

    private final ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();

    /**
     * Constructor
     */
    public DictionaryPrimitrivesRWLock() {
        super();
        this.triples = new HashMap<>();
        this.counter = 0;
        this.primitivesCounter = -1;

        this.initialize();
    }

    @Override
    public final long add(final String s) {
        Long id = null;
        try {
            this.rwlock.writeLock().lock();
            final Node n = NodeFactory.createURI(s);
            if (this.triples.containsKey(n)) {
                id = this.get(n);
            } else {
                /* Look for primitives */
                if (s.matches("(\".*\")\\^\\^.*")) {
                    this.triples.put(n, this.primitivesCounter);
                    id = this.primitivesCounter--;
                } else {
                    this.triples.put(n, this.counter);
                    id = this.counter++;
                }
            }
        } catch (final Exception e) {
            LOGGER.error("", e);
        } finally {
            this.rwlock.writeLock().unlock();
        }
        return id;
    }

    @Override
    public final long add(final Node n) {
        Long id = null;
        try {
            this.rwlock.writeLock().lock();
            if (this.triples.containsKey(n)) {
                id = this.get(n);
            } else {
                /* Look for primitives */
                if (n.toString().matches("(\".*\")\\^\\^.*")) {
                    this.triples.put(n, this.primitivesCounter);
                    id = this.primitivesCounter--;
                } else {
                    this.triples.put(n, this.counter);
                    id = this.counter++;
                }
            }
        } catch (final Exception e) {
            LOGGER.error("", e);
        } finally {
            this.rwlock.writeLock().unlock();
        }
        return id;
    }

    @Override
    public final Node get(final long index) {
        Node value = null;
        try {
            this.rwlock.readLock().lock();
            final Iterator<Entry<Node, Long>> it = this.triples.entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry<Node, Long> pairs = it.next();
                if (pairs.getValue().equals(index)) {
                    value = pairs.getKey();
                    break;
                }
            }
        } catch (final Exception e) {
            LOGGER.error("", e);
        } finally {
            this.rwlock.readLock().unlock();
        }
        return value;
    }

    @Override
    public final long get(final String s) {
        Long id = null;
        try {
            this.rwlock.readLock().lock();
            id = this.triples.get(NodeFactory.createURI(s));
        } catch (final Exception e) {
            LOGGER.error("", e);
        } finally {
            this.rwlock.readLock().unlock();
        }
        return id;
    }

    @Override
    public final long get(final Node n) {
        Long id = null;
        try {
            this.rwlock.readLock().lock();
            id = this.triples.get(n);
        } catch (final Exception e) {
            LOGGER.error("", e);
        } finally {
            this.rwlock.readLock().unlock();
        }
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
        result = prime * result + (int) (this.counter ^ this.counter >>> 32);
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
        final DictionaryPrimitrivesRWLock other = (DictionaryPrimitrivesRWLock) obj;
        if (this.counter != other.counter) {
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

    @Override
    public String printDico() {
        final StringBuilder sb = new StringBuilder();
        try {
            this.rwlock.readLock().lock();
            sb.append("\n");
            for (final Node n : this.triples.keySet()) {
                sb.append(this.triples.get(n.toString()));
                sb.append("=");
                sb.append(this.printAxiom(n.toString()));
                sb.append("\n");
            }
        } catch (final Exception e) {
            LOGGER.error("", e);
        } finally {
            this.rwlock.readLock().unlock();
        }
        this.rwlock.readLock().unlock();
        return sb.toString();
    }

    @Override
    public String printTriple(final Triple t) {
        String s = null, p = null, o = null;
        try {
            this.rwlock.readLock().lock();
            s = this.printAxiom(this.get(t.getSubject()).toString());
            p = this.printAxiom(this.get(t.getPredicate()).toString());
            o = this.printAxiom(this.get(t.getObject()).toString());
        } catch (final Exception e) {
            LOGGER.error("", e);
        } finally {
            this.rwlock.readLock().unlock();
        }

        return s + " " + p + " " + o;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        try {
            this.rwlock.readLock().lock();
            for (final Node n : this.triples.keySet()) {
                sb.append(n + " ==> " + this.printAxiom(n.toString()) + "\n");
            }
        } catch (final Exception e) {
            LOGGER.error("", e);
        } finally {
            this.rwlock.readLock().unlock();
        }
        return sb.toString();
    }

}
