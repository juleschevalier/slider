package fr.ujm.tse.lt2c.satin.dictionary.test;

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

import org.junit.Assert;
import org.junit.Test;

import fr.ujm.tse.lt2c.satin.slider.dictionary.DictionaryPrimitrivesRWLock;
import fr.ujm.tse.lt2c.satin.slider.interfaces.Dictionary;

public class TestDictionnary {

    @Test
    public void testSize() {

        final Dictionary dico = new DictionaryPrimitrivesRWLock();

        final long sizeBefore = dico.size();

        final String s = "Hello world";
        dico.add(s);

        Assert.assertEquals(sizeBefore + 1, dico.size());

    }

    @Test
    public void testAddGet() {

        final Dictionary dico = new DictionaryPrimitrivesRWLock();

        final String s1 = "Hello world";
        final long l1 = dico.add(s1);

        Assert.assertEquals(l1, dico.get(s1));
        Assert.assertEquals(s1, dico.get(l1));

        final long l2 = dico.add(s1);
        Assert.assertEquals(l1, l2);

        final String s2 = "Goodbye world";
        final long l3 = dico.add(s2);
        Assert.assertNotSame(l1, l3);

    }

}
