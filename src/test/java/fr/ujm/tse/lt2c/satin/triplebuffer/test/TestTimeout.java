package fr.ujm.tse.lt2c.satin.triplebuffer.test;

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

import org.junit.Test;

import fr.ujm.tse.lt2c.satin.slider.buffer.QueuedTripleBufferLock;
import fr.ujm.tse.lt2c.satin.slider.dictionary.AbstractDictionary;
import fr.ujm.tse.lt2c.satin.slider.dictionary.DictionaryPrimitrivesRWLock;
import fr.ujm.tse.lt2c.satin.slider.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.slider.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.slider.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.slider.reasoner.IncrementalReasoner;
import fr.ujm.tse.lt2c.satin.slider.rules.Ruleset;
import fr.ujm.tse.lt2c.satin.slider.triplestore.ImmutableTriple;
import fr.ujm.tse.lt2c.satin.slider.triplestore.VerticalPartioningTripleStoreRWLock;

public class TestTimeout {

    @Test
    public void test() {

        final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        final Dictionary dictionary = new DictionaryPrimitrivesRWLock();
        final IncrementalReasoner reasoner = new IncrementalReasoner(tripleStore, dictionary, Ruleset.RHODF, IncrementalReasoner.DEFAULT_THREADS_NB,
                QueuedTripleBufferLock.DEFAULT_BUFFER_SIZE, 1000);

        reasoner.start();
        /****************/

        final Collection<Triple> triples = new HashSet<>();

        /* Add subproperties */
        long a = 1, b = -1;
        for (int i = 0; i < 500; i++) {
            triples.add(new ImmutableTriple(a++, AbstractDictionary.subPropertyOf, b--));
        }
        tripleStore.addAll(triples);
        reasoner.addTriples(triples);
        triples.clear();

        /* Add subclasses */
        for (int j = 0; j < 1000; j++) {
            for (int i = 0; i < 1000; i++) {
                triples.add(new ImmutableTriple(a++, AbstractDictionary.subClassOf, b--));
            }
            tripleStore.addAll(triples);
            reasoner.addTriples(triples);
            triples.clear();
        }

        /* Add subproperties */
        for (int i = 0; i < 500; i++) {
            triples.add(new ImmutableTriple(a++, AbstractDictionary.subPropertyOf, b--));
        }
        tripleStore.addAll(triples);
        reasoner.addTriples(triples);
        triples.clear();

        /* Add subclasses */
        for (int j = 0; j < 1000; j++) {
            for (int i = 0; i < 1000; i++) {
                triples.add(new ImmutableTriple(a++, AbstractDictionary.subClassOf, b--));
            }
            tripleStore.addAll(triples);
            reasoner.addTriples(triples);
            triples.clear();
        }

        /****************/
        reasoner.closeAndWait();

        System.out.println("Total: " + tripleStore.size());
        System.out.println("SCO: " + tripleStore.getbyPredicate(AbstractDictionary.subClassOf).size());
        System.out.println("SPO: " + tripleStore.getbyPredicate(AbstractDictionary.subPropertyOf).size());
    }
}
