package fr.ujm.tse.lt2c.satin.triplestore.test;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

public class TestVerticalPartitioningTripleStoreRWLock {

	@Test
	public void test() {
		TripleStore ts = new VerticalPartioningTripleStoreRWLock();
		Set<Triple> generated = new HashSet<>();
		Random random = new Random();
		for (int i = 0; i < 10000; i++) {
			Triple t = new ImmutableTriple(random.nextInt(10),
					random.nextInt(10), random.nextInt(10));
			ts.add(t);
			generated.add(t);
		}
		assertEquals(ts.size(), generated.size());
		assertEquals(ts.getAll().size(), generated.size());

	}

}
