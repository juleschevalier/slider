package fr.ujm.tse.lt2c.satin.buffer;

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

import java.util.Collection;
import java.util.HashSet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;

/**
 * Get triples and send them to subscribers depends on their predicate
 * 
 * @author Jules Chevalier
 * @see Triple, TripleBuffer
 */
public class TripleDistributor {

    private final Multimap<Long, TripleBuffer> subscribers;
    private final Collection<TripleBuffer> universalSubscribers;

    /**
     * Constructor
     */
    public TripleDistributor() {
        super();
        this.subscribers = HashMultimap.create();
        this.universalSubscribers = new HashSet<>();
    }

    /**
     * Add a TripleBuffer as subscribers for predicates
     * 
     * @param tripleBuffer
     * @param predicates
     * @see TripleBuffer
     */
    public void addSubscriber(final TripleBuffer tripleBuffer, final long[] predicates) {
        if (predicates.length == 0) {
            if (!this.universalSubscribers.contains(tripleBuffer)) {
                this.universalSubscribers.add(tripleBuffer);
            }
            return;
        }
        for (final Long predicate : predicates) {
            if (!this.subscribers.containsEntry(predicate, tripleBuffer)) {
                this.subscribers.put(predicate, tripleBuffer);
            }
        }
    }

    /**
     * Send all the triples to all matching subscribers
     * 
     * @param triples
     * @see Triple
     */
    public void distributeAll(final Collection<Triple> triples) {
        for (final Triple triple : triples) {
            this.ditribute(triple);
        }
    }

    /**
     * Send a triple to all matching subscribers
     * 
     * @param triple
     * @return the number of times a triple has been sent
     */
    public long ditribute(final Triple triple) {
        long debugDistributed = 0;
        for (final TripleBuffer tripleBuffer : this.subscribers.get(triple.getPredicate())) {
            tripleBuffer.add(triple);
            debugDistributed++;
        }
        for (final TripleBuffer tripleBuffer : this.universalSubscribers) {
            tripleBuffer.add(triple);
            debugDistributed++;
        }
        return debugDistributed;
    }

    /**
     * @return the number of subscribers
     */
    public int subscribersNumber() {
        return this.subscribers.values().size() + this.universalSubscribers.size();
    }

}
