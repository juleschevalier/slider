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
import java.util.Map.Entry;
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
 * c1 rdfs:subClassOf c2
 * OUPUT
 * c1 rdfs:subClassOf c1
 * c2 rdfs:subClassOf c2
 * 
 * @author Jules Chevalier
 *
 */
public class RunRHODF7a extends AbstractRun {

    private static final Logger LOGGER = Logger.getLogger(RunRHODF7a.class);
    private static final String RULENAME = "RHODF7a";
    public static final long[] INPUT_MATCHERS = { AbstractDictionary.subClassOf };
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.subClassOf };

    public RunRHODF7a(final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer, final TripleDistributor tripleDistributor,
            final AtomicInteger phaser) {
        super(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        super.ruleName = RULENAME;
        super.complexity = 1;
    }

    @Override
    protected int process(final TripleStore ts1, final TripleStore ts2, final Collection<Triple> outputTriples) {

        final long subClassOf = AbstractDictionary.subClassOf;

        int loops = 0;

        final Multimap<Long, Long> subpropertyMultiMap = ts2.getMultiMapForPredicate(subClassOf);
        if (subpropertyMultiMap != null && !subpropertyMultiMap.isEmpty()) {
            for (final Entry<Long, Long> entry : subpropertyMultiMap.entries()) {
                loops++;
                final Triple result1 = new ImmutableTriple(entry.getKey(), subClassOf, entry.getKey());
                final Triple result2 = new ImmutableTriple(entry.getValue(), subClassOf, entry.getValue());
                outputTriples.add(result1);
                outputTriples.add(result2);
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
