package fr.ujm.tse.lt2c.satin.tripledistributor.test;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Test;

import fr.ujm.tse.lt2c.satin.buffer.TripleBufferLock;
import fr.ujm.tse.lt2c.satin.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;

public class TestTripleDistributor {

    @Test
    public void test() {

        TripleBuffer tb = new TripleBufferLock();
        TripleDistributor td = new TripleDistributor();

        td.addSubscriber(tb, new long[] { 1, 2, 3 });

        assertEquals(tb.mainBufferOccupation() + tb.secondaryBufferOccupation(), 0);

        /* Test add matching */
        Collection<Triple> triples = new HashSet<>();
        triples.add(new ImmutableTriple(0, 2, 0));

        td.distribute(triples);

        assertEquals(tb.mainBufferOccupation(), 1);
        assertEquals(tb.secondaryBufferOccupation(), 0);

        /* Test add non-matching */
        triples.add(new ImmutableTriple(1, 0, 0));

        td.distribute(triples);

        assertEquals(tb.mainBufferOccupation(), 1);
        assertEquals(tb.secondaryBufferOccupation(), 0);

        /* Test multi subscribing */
        td = new TripleDistributor();
        tb = new TripleBufferLock();
        TripleBuffer tb2 = new TripleBufferLock();
        triples = new HashSet<>();

        td.addSubscriber(tb, new long[] { 1 });
        td.addSubscriber(tb2, new long[] { 2 });

        triples.add(new ImmutableTriple(0, 0, 0));
        triples.add(new ImmutableTriple(0, 1, 1));
        triples.add(new ImmutableTriple(0, 2, 2));
        triples.add(new ImmutableTriple(0, 3, 3));
        triples.add(new ImmutableTriple(0, 1, 4));
        td.distribute(triples);

        assertEquals(tb.mainBufferOccupation(), 2);
        assertEquals(tb.secondaryBufferOccupation(), 0);

        assertEquals(tb2.mainBufferOccupation(), 1);
        assertEquals(tb2.secondaryBufferOccupation(), 0);

    }

}
