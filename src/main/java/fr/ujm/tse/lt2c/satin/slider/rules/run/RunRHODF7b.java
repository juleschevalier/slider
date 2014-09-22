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
 * x p y (p in {rdfs:domain, rdfs:range, rdf:type}
 * OUPUT
 * x rdfs:subClassOf x
 * 
 * @author Jules Chevalier
 *
 */
public class RunRHODF7b extends AbstractRun {

    private static final Logger LOGGER = Logger.getLogger(RunRHODF7b.class);
    private static final String RULENAME = "RHODF7b";
    public static final long[] INPUT_MATCHERS = { AbstractDictionary.domain, AbstractDictionary.range, AbstractDictionary.type };
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.subPropertyOf };

    public RunRHODF7b(final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer, final TripleDistributor tripleDistributor,
            final AtomicInteger phaser) {
        super(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        super.ruleName = RULENAME;
        super.complexity = 1;
    }

    @Override
    protected int process(final TripleStore ts1, final TripleStore ts2, final Collection<Triple> outputTriples) {

        final long domain = AbstractDictionary.domain;
        final long range = AbstractDictionary.range;
        final long type = AbstractDictionary.type;
        final long subClassOf = AbstractDictionary.subClassOf;

        int loops = 0;

        final Multimap<Long, Long> domainMultiMap = ts2.getMultiMapForPredicate(domain);
        final Multimap<Long, Long> rangeMultiMap = ts2.getMultiMapForPredicate(range);
        final Multimap<Long, Long> typeMultiMap = ts2.getMultiMapForPredicate(type);
        if (domainMultiMap != null && !domainMultiMap.isEmpty()) {
            for (final Long o : domainMultiMap.values()) {
                loops++;
                final Triple result = new ImmutableTriple(o, subClassOf, o);
                outputTriples.add(result);
            }
        }
        if (rangeMultiMap != null && !rangeMultiMap.isEmpty()) {
            for (final Long o : rangeMultiMap.values()) {
                loops++;
                final Triple result = new ImmutableTriple(o, subClassOf, o);
                outputTriples.add(result);
            }
        }
        if (typeMultiMap != null && !typeMultiMap.isEmpty()) {
            for (final Long o : typeMultiMap.values()) {
                loops++;
                final Triple result = new ImmutableTriple(o, subClassOf, o);
                outputTriples.add(result);
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
