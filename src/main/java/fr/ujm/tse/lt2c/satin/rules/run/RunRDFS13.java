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
 * x rdf:type rdfs:Datatype
 * OUPUT
 * x rdfs:subClassOf rdfs:Literal
 * 
 * @author Jules Chevalier
 *
 */
public class RunRDFS13 extends AbstractRun {

    private static final Logger LOGGER = Logger.getLogger(RunRDFS13.class);
    private static final String RULENAME = "RDFS13";
    public static final long[] INPUT_MATCHERS = { AbstractDictionary.type };
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.type };

    public RunRDFS13(final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer, final TripleDistributor tripleDistributor,
            final AtomicInteger phaser) {
        super(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        super.ruleName = RULENAME;
        super.complexity = 1;
    }

    @Override
    protected int process(final TripleStore ts1, final TripleStore ts2, final Collection<Triple> outputTriples) {

        final long datatype = AbstractDictionary.datatype;
        final long type = AbstractDictionary.type;
        final long subClassOf = AbstractDictionary.subClassOf;
        final long literal = AbstractDictionary.literal;

        int loops = 0;

        final Multimap<Long, Long> typeMultimap = ts1.getMultiMapForPredicate(type);
        if (typeMultimap != null && !typeMultimap.isEmpty()) {
            for (final Long subject : typeMultimap.keySet()) {
                if (typeMultimap.get(subject).contains(datatype)) {
                    loops++;
                    final Triple result = new ImmutableTriple(subject, subClassOf, literal);
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
