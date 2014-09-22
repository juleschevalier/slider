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

import org.junit.Test;

import fr.ujm.tse.lt2c.satin.slider.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.slider.triplestore.ImmutableTriple;
import fr.ujm.tse.lt2c.satin.slider.triplestore.VerticalPartioningTripleStoreRWLock;

public class TestGetVerticalPartitioningTripleStoreRWLock {

    @Test
    public void test() {
        TripleStore ts = new VerticalPartioningTripleStoreRWLock();

        ts.add(new ImmutableTriple(1, 2, 3));
        ts.add(new ImmutableTriple(3, 2, 1));
        ts.add(new ImmutableTriple(4, 5, 6));

        assertEquals(ts.getbyObject(3).size(), 1);
        assertEquals(ts.getbyPredicate(2).size(), 2);
        assertEquals(ts.getbySubject(1).size(), 1);
        assertEquals(ts.getMultiMapForPredicate(2).size(), 2);
        assertEquals(ts.getMultiMapForPredicate(5).size(), 1);

        assertEquals(ts.getbyObject(7).size(), 0);
        assertEquals(ts.getbyPredicate(7).size(), 0);
        assertEquals(ts.getbySubject(7).size(), 0);
        assertEquals(ts.getMultiMapForPredicate(7), null);

    }

}
