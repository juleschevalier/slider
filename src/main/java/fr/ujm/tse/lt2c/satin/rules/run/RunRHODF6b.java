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
import java.util.Map.Entry;
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
 * p1 rdfs:subPropertyOf p2
 * OUPUT
 * p1 rdfs:subPropertyOf p1
 * p2 rdfs:subPropertyOf p2
 * 
 * @author Jules Chevalier
 *
 */
public class RunRHODF6b extends AbstractRun {

    private static final Logger LOGGER = Logger.getLogger(RunRHODF6b.class);
    private static final String RULENAME = "RHODF6b";
    public static final long[] INPUT_MATCHERS = { AbstractDictionary.subPropertyOf };
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.subPropertyOf };

    public RunRHODF6b(final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer, final TripleDistributor tripleDistributor,
            final AtomicInteger phaser) {
        super(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        super.ruleName = RULENAME;
        super.complexity = 1;
    }

    @Override
    protected int process(final TripleStore ts1, final TripleStore ts2, final Collection<Triple> outputTriples) {

        final long subPropertyOf = AbstractDictionary.subPropertyOf;

        int loops = 0;

        final Multimap<Long, Long> subpropertyMultimap = ts2.getMultiMapForPredicate(subPropertyOf);
        if (subpropertyMultimap != null && !subpropertyMultimap.isEmpty()) {
            for (final Entry<Long, Long> entry : subpropertyMultimap.entries()) {
                loops++;
                final Triple result1 = new ImmutableTriple(entry.getKey(), subPropertyOf, entry.getKey());
                final Triple result2 = new ImmutableTriple(entry.getValue(), subPropertyOf, entry.getValue());
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
