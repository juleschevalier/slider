package fr.ujm.tse.lt2c.satin.triplestore.test;

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

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import fr.ujm.tse.lt2c.satin.slider.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.slider.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.slider.triplestore.ImmutableTriple;
import fr.ujm.tse.lt2c.satin.slider.triplestore.VerticalPartioningTripleStoreRWLock;

public class TestVerticalPartitioningTripleStoreRWLock {

    @Test
    public void test() {
        TripleStore ts = new VerticalPartioningTripleStoreRWLock();
        Set<Triple> generated = new HashSet<>();
        Random random = new Random();
        for (int i = 0; i < 10000; i++) {
            Triple t = new ImmutableTriple(random.nextInt(10), random.nextInt(10), random.nextInt(10));
            ts.add(t);
            generated.add(t);
        }
        assertEquals(ts.size(), generated.size());
        assertEquals(ts.getAll().size(), generated.size());

    }

}
