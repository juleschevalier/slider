package fr.ujm.tse.lt2c.satin.rules.run;

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
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.google.common.collect.Multimap;

import fr.ujm.tse.lt2c.satin.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.dictionary.AbstractDictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;

/**
 * INPUT
 * c1 rdfs:subPropertyOf c2
 * c2 rdfs:subPropertyOf c1
 * OUPUT
 * c1 owl:equivalentProperty c2
 */
public class RunSCM_EQP2 extends AbstractRun {

    private static final Logger LOGGER = Logger.getLogger(RunSCM_EQP2.class);
    private static final String ruleName = "SCM_EQP2";
    public static final long[] INPUT_MATCHERS = { AbstractDictionary.subPropertyOf };
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.equivalentProperty };

    public RunSCM_EQP2(final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer, final TripleDistributor tripleDistributor,
            final AtomicInteger phaser) {
        super(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
    }

    @Override
    protected int process(final TripleStore ts1, final TripleStore ts2, final Collection<Triple> outputTriples) {

        final long subPropertyOf = AbstractDictionary.subPropertyOf;
        final long equivalentProperty = AbstractDictionary.equivalentProperty;

        int loops = 0;

        final Multimap<Long, Long> subpropertyMultimap = ts1.getMultiMapForPredicate(subPropertyOf);
        if ((subpropertyMultimap != null) && !subpropertyMultimap.isEmpty()) {

            final Collection<Triple> subpropertyTriples = ts2.getbyPredicate(subPropertyOf);

            final HashMap<Long, Collection<Long>> cachePredicates = new HashMap<>();

            /* For each type triple */
            for (final Triple triple : subpropertyTriples) {
                /*
                 * Get all objects (c1a) of subPropertyOf triples with
                 */

                Collection<Long> c1as;
                if (!cachePredicates.containsKey(triple.getObject())) {
                    c1as = subpropertyMultimap.get(triple.getObject());
                    cachePredicates.put(triple.getObject(), c1as);
                } else {
                    c1as = cachePredicates.get(triple.getObject());
                }

                loops++;
                for (final Long c1a : c1as) {

                    if ((c1a == triple.getSubject()) && (triple.getObject() != triple.getSubject())) {

                        final Triple result = new ImmutableTriple(triple.getSubject(), equivalentProperty, triple.getObject());
                        outputTriples.add(result);
                        // if (logger.isTraceEnabled()) {
                        // logger.trace(dictionary.printTriple(new ImmutableTriple(triple.getSubject(), subPropertyOf,
                        // triple.getObject())) + " & " + dictionary.printTriple(new ImmutableTriple(triple.getObject(),
                        // subPropertyOf, triple.getSubject())) + " -> " + dictionary.printTriple(result));
                        // }
                    }
                }
            }
        }

        return loops;

    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public String toString() {
        return this.ruleName;
    }

}
