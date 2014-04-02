package fr.ujm.tse.lt2c.satin.tripledistributor.test;

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

import org.junit.Test;

public class TestTripleDistributor {

    @Test
    public void test() {
        //
        // TripleBuffer tb = new TripleBufferLock();
        // TripleDistributor td = new TripleDistributor();
        //
        // td.addSubscriber(tb, new long[] { 1, 2, 3 });
        //
        // assertEquals(tb.mainBufferOccupation() +
        // tb.secondaryBufferOccupation(), 0);
        //
        // /* Test add matching */
        // TripleStore triples = new VerticalPartioningTripleStoreRWLock();
        // triples.add(new ImmutableTriple(0, 2, 0));
        //
        // td.distributeAll(triples);
        //
        // assertEquals(tb.mainBufferOccupation(), 1);
        // assertEquals(tb.secondaryBufferOccupation(), 0);
        //
        // /* Test add non-matching */
        // triples.add(new ImmutableTriple(1, 0, 0));
        //
        // td.distributeAll(triples);
        //
        // assertEquals(tb.mainBufferOccupation(), 1);
        // assertEquals(tb.secondaryBufferOccupation(), 0);
        //
        // /* Test multi subscribing */
        // td = new TripleDistributor();
        // tb = new TripleBufferLock();
        // final TripleBuffer tb2 = new TripleBufferLock();
        // triples = new VerticalPartioningTripleStoreRWLock();
        //
        // td.addSubscriber(tb, new long[] { 1 });
        // td.addSubscriber(tb2, new long[] { 2 });
        //
        // triples.add(new ImmutableTriple(0, 0, 0));
        // triples.add(new ImmutableTriple(0, 1, 1));
        // triples.add(new ImmutableTriple(0, 2, 2));
        // triples.add(new ImmutableTriple(0, 3, 3));
        // triples.add(new ImmutableTriple(0, 1, 4));
        // td.distributeAll(triples);
        //
        // assertEquals(tb.mainBufferOccupation(), 2);
        // assertEquals(tb.secondaryBufferOccupation(), 0);
        //
        // assertEquals(tb2.mainBufferOccupation(), 1);
        // assertEquals(tb2.secondaryBufferOccupation(), 0);
        //
    }

}
