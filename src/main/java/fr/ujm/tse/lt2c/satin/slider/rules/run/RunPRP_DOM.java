package fr.ujm.tse.lt2c.satin.slider.rules.run;

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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.google.common.collect.Multimap;

import fr.ujm.tse.lt2c.satin.slider.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.slider.dictionary.AbstractDictionary;
import fr.ujm.tse.lt2c.satin.slider.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.slider.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.slider.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.slider.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.slider.triplestore.ImmutableTriple;

/**
 * INPUT
 * p rdfs:domain c
 * x p y
 * OUPUT
 * x rdf:type c
 * 
 * @author Jules Chevalier
 *
 */
public class RunPRP_DOM extends AbstractRun {

    private static final Logger LOGGER = Logger.getLogger(RunPRP_DOM.class);
    private static final String RULENAME = "PRP_DOM";
    public static final long[] INPUT_MATCHERS = {};
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.type };

    public RunPRP_DOM(final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer, final TripleDistributor tripleDistributor,
            final AtomicInteger phaser) {
        super(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        super.ruleName = RULENAME;

    }

    @Override
    protected int process(final TripleStore ts1, final TripleStore ts2, final Collection<Triple> outputTriples) {

        final long domain = AbstractDictionary.domain;
        final long type = AbstractDictionary.type;

        final int loops = 0;

        final Multimap<Long, Long> domainMultiMap = ts1.getMultiMapForPredicate(domain);
        if (domainMultiMap != null && !domainMultiMap.isEmpty()) {

            final Map<Long, Collection<Triple>> cachePredicates = new HashMap<>();

            for (final Long p : domainMultiMap.keySet()) {

                Collection<Triple> matchingTriples;
                if (!cachePredicates.containsKey(p)) {
                    matchingTriples = ts2.getbyPredicate(p);
                    cachePredicates.put(p, matchingTriples);
                } else {
                    matchingTriples = cachePredicates.get(p);
                }

                for (final Triple triple : matchingTriples) {

                    for (final Long c : domainMultiMap.get(p)) {

                        if (triple.getSubject() >= 0) {
                            final Triple result = new ImmutableTriple(triple.getSubject(), type, c);
                            outputTriples.add(result);
                        }
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
        return RULENAME;
    }

}