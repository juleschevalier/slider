package fr.ujm.tse.lt2c.satin.dictionary.test;

import org.junit.Assert;
import org.junit.Test;

import fr.ujm.tse.lt2c.satin.dictionary.DictionaryPrimitrivesRWLock;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;

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
