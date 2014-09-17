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
 * p2 rdfs:range c
 * p1 rdfs:subPropertyOf p2
 * OUPUT
 * p1 rdfs:range c
 * 
 * @author Jules Chevalier
 *
 */
public class RunSCM_RNG2 extends AbstractRun {

    private static final Logger LOGGER = Logger.getLogger(RunSCM_RNG2.class);
    private static final String RULENAME = "SCM_RNG2";
    public static final long[] INPUT_MATCHERS = { AbstractDictionary.range, AbstractDictionary.subPropertyOf };
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.range };

    public RunSCM_RNG2(final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer, final TripleDistributor tripleDistributor,
            final AtomicInteger phaser) {
        super(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        super.ruleName = RULENAME;

    }

    @Override
    protected int process(final TripleStore ts1, final TripleStore ts2, final Collection<Triple> outputTriples) {

        final long subPropertyOf = AbstractDictionary.subPropertyOf;
        final long range = AbstractDictionary.range;

        int loops = 0;

        final Multimap<Long, Long> rangeMultimap = ts1.getMultiMapForPredicate(range);
        if (rangeMultimap != null && !rangeMultimap.isEmpty()) {

            final Collection<Triple> subpropertyTriples = ts2.getbyPredicate(subPropertyOf);

            final HashMap<Long, Collection<Long>> cachePredicates = new HashMap<>();

            /* For each type triple */
            for (final Triple triple : subpropertyTriples) {
                /*
                 * Get all objects (c2) of subClassOf triples with range triples
                 * objects as subject
                 */

                Collection<Long> cs;
                if (!cachePredicates.containsKey(triple.getObject())) {
                    cs = rangeMultimap.get(triple.getObject());
                    cachePredicates.put(triple.getObject(), cs);
                } else {
                    cs = cachePredicates.get(triple.getObject());
                }

                loops++;
                for (final Long c : cs) {

                    final Triple result = new ImmutableTriple(triple.getSubject(), range, c);
                    outputTriples.add(result);
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
        return RULENAME;
    }

}
