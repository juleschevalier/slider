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

import org.junit.Test;

import fr.ujm.tse.lt2c.satin.slider.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.slider.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.slider.triplestore.ImmutableTriple;
import fr.ujm.tse.lt2c.satin.slider.triplestore.VerticalPartioningTripleStoreRWSmartLock;

public class TestRemove {

    @Test
    public void test() {

        final TripleStore ts = new VerticalPartioningTripleStoreRWSmartLock();

        final Triple t1 = new ImmutableTriple(0, 1, 2);
        final Triple t2 = new ImmutableTriple(4, 5, 6);
        final Triple t3 = new ImmutableTriple(7, 8, 9);

        System.out.println("add " + t1);
        ts.add(t1);
        System.out.println("ts " + ts.size() + " " + ts.getAll());
        System.out.println("add " + t2);
        ts.add(t2);
        System.out.println("ts " + ts.size() + " " + ts.getAll());
        System.out.println("remove " + t1);
        ts.remove(t1);
        System.out.println("ts " + ts.size() + " " + ts.getAll());
        System.out.println("remove " + t3);
        ts.remove(t3);
        System.out.println("ts " + ts.size() + " " + ts.getAll());

    }

}
