package fr.ujm.tse.lt2c.satin.triplestore.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

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
