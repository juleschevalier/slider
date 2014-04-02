package fr.ujm.tse.lt2c.satin.dictionary;

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

import fr.ujm.tse.lt2c.satin.interfaces.Triple;

/**
 * Concurrent implementation of the {@link interfaces.Dictionary}.
 * Literals and primitives are stores with negative id, concepts with positive ones
 * 
 * @author Jules Chevalier
 * 
 */
public class DictionaryPrimitrivesRWLock extends AbstractDictionary {
    private static Logger logger = Logger.getLogger(DictionaryPrimitrivesRWLock.class);

    private Map<String, Long> triples = new HashMap<>();
    long counter;
    long primitivesCounter;

    ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();

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
            if (this.triples.containsKey(s)) {
                id = this.get(s);
            } else {
                /* Look for primitives */
                if (s.matches("(\".*\")\\^\\^.*")) {
                    this.triples.put(s, this.primitivesCounter);
                    id = this.primitivesCounter--;
                } else {
                    this.triples.put(s, this.counter);
                    id = this.counter++;
                }
            }
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.rwlock.writeLock().unlock();
        }
        return id;
    }

    @Override
    public final String get(final long index) {
        String value = null;
        try {
            this.rwlock.readLock().lock();
            final Iterator<Entry<String, Long>> it = this.triples.entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry<String, Long> pairs = it.next();
                if (pairs.getValue().equals(index)) {
                    value = pairs.getKey();
                    break;
                }
            }
        } catch (final Exception e) {
            logger.error("", e);
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
            id = this.triples.get(s);
        } catch (final Exception e) {
            logger.error("", e);
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
        result = (prime * result) + (int) (this.counter ^ (this.counter >>> 32));
        result = (prime * result) + ((this.triples == null) ? 0 : this.triples.hashCode());
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
            for (final String s : this.triples.keySet()) {
                sb.append(this.triples.get(s));
                sb.append("=");
                sb.append(this.printConcept(s));
                sb.append("\n");
            }
        } catch (final Exception e) {
            logger.error("", e);
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
            s = this.printConcept(this.get(t.getSubject()));
            p = this.printConcept(this.get(t.getPredicate()));
            o = this.printConcept(this.get(t.getObject()));
        } catch (final Exception e) {
            logger.error("", e);
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
            for (final String s : this.triples.keySet()) {
                sb.append(s + " ==> " + this.printConcept(s) + "\n");
            }
        } catch (final Exception e) {
            logger.error("", e);
        } finally {
            this.rwlock.readLock().unlock();
        }
        return sb.toString();
    }

}
