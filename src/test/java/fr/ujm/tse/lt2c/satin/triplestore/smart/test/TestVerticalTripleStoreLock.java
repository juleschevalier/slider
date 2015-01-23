package fr.ujm.tse.lt2c.satin.triplestore.smart.test;

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

import junit.framework.Assert;

import org.junit.Test;

import fr.ujm.tse.lt2c.satin.slider.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.slider.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.slider.triplestore.ImmutableTriple;
import fr.ujm.tse.lt2c.satin.slider.triplestore.VerticalPartioningTripleStoreRWSmartLock;

public class TestVerticalTripleStoreLock {

    @Test
    public void testAdd() {
        final long s = 1l, p = 2l, o = 3l;
        final Triple t1 = new ImmutableTriple(s, p, o);
        final TripleStore ts = new VerticalPartioningTripleStoreRWSmartLock();

        ts.add(t1);

        final Triple t2 = ts.getAll().iterator().next();

        Assert.assertEquals(t1, t2);
    }

    @Test
    public void testGet() {
        final long s1 = 1l, p1 = 2l, o1 = 3l;
        final long s2 = 4l, p2 = 5l, o2 = 6l;
        final Triple t1 = new ImmutableTriple(s1, p1, o1);
        final Triple t2 = new ImmutableTriple(s2, p2, o2);
        final TripleStore ts = new VerticalPartioningTripleStoreRWSmartLock();

        ts.add(t1);
        ts.add(t2);

        Collection<Triple> triples = ts.getbySubject(s1);

        for (final Triple triple : triples) {
            Assert.assertEquals(triple.getSubject(), s1);
        }

        triples = ts.getbyPredicate(p1);

        for (final Triple triple : triples) {
            Assert.assertEquals(triple.getPredicate(), p1);
        }

        triples = ts.getbyObject(o1);

        for (final Triple triple : triples) {
            Assert.assertEquals(triple.getObject(), o1);
        }

    }

    @Test
    public void testUnicity() {
        final TripleStore tripleStore = new VerticalPartioningTripleStoreRWSmartLock();

        final Triple t = new ImmutableTriple(1l, 2l, 3l);

        tripleStore.add(t);
        tripleStore.add(t);

        Assert.assertEquals(1, tripleStore.size());

    }

    @Test
    public void testGetByPredicates() {
        final TripleStore tripleStore = new VerticalPartioningTripleStoreRWSmartLock();

        tripleStore.add(new ImmutableTriple(0, 0, 0));
        tripleStore.add(new ImmutableTriple(0, 0, 1));
        tripleStore.add(new ImmutableTriple(0, 0, 2));
        tripleStore.add(new ImmutableTriple(0, 1, 0));

        Assert.assertEquals(tripleStore.getbyPredicate(0).size(), 3);
        Assert.assertEquals(tripleStore.getbyPredicate(1).size(), 1);

    }

}
