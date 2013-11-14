package fr.ujm.tse.lt2c.satin.triplebuffer.test;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import fr.ujm.tse.lt2c.satin.buffer.TripleBufferLock;
import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;

public class TestTripleBuffer {

	public final static int PROBA = 1000000;

	@Test
	public void test() {
		TripleBuffer tb = new TripleBufferLock();
		Set<Triple> generated = new HashSet<>();
		
		// Test buffer flush
		Random random = new Random();
		while (generated.size() < tb.getBufferLimit() - 1) {
			Triple t = new ImmutableTriple(random.nextInt(PROBA), random.nextInt(PROBA), random.nextInt(PROBA));
			while (!tb.add(t))
				;
			generated.add(t);
		}
		assertEquals(generated.size(), tb.flush().size());
		
		// ----Clear test
		SimpleBufferListener sbl = new SimpleBufferListener(tb);
		tb.addBufferListener(sbl);
		assertEquals(1, tb.getBufferListeners().size());
		// Switchy must occur here
		tb.add(new ImmutableTriple(random.nextInt(PROBA), random.nextInt(PROBA), random.nextInt(PROBA)));
//		System.out.println(tb.mainBufferOccupation()+" "+tb.secondaryBufferOccupation());
		assertEquals(1, tb.mainBufferOccupation());
		assertEquals(0, tb.secondaryBufferOccupation());
		
		// ---- Overflow test
		tb = new TripleBufferLock();
		OverFlowListener ofl = new OverFlowListener(tb);
		tb.addBufferListener(ofl);
		assertEquals(1, tb.getBufferListeners().size());
		generated.clear();
		while (generated.size() < (tb.getBufferLimit() * 3 + 3)) {
			Triple t = new ImmutableTriple(random.nextInt(PROBA), random.nextInt(PROBA), random.nextInt(PROBA));
			boolean ok=false;while (!tb.add(t)){System.out.print(".");ok=true;}
			if(ok){System.out.println();}
			generated.add(t);
		}
		assertEquals(3, tb.mainBufferOccupation());
		assertEquals(0, tb.secondaryBufferOccupation());

	}

	class SimpleBufferListener implements BufferListener {

		TripleBuffer tb;

		public SimpleBufferListener(TripleBuffer tb) {
			super();
			this.tb = tb;
		}

		@Override
		public void bufferFull() {
//			System.out.println("BufferFull called");
			assertEquals(0, tb.mainBufferOccupation());
			assertEquals(tb.getBufferLimit(), tb.secondaryBufferOccupation());
			TripleStore emptyedBuffer = tb.clear();
			assertEquals(emptyedBuffer.size(), tb.getBufferLimit());
			assertEquals(0, tb.mainBufferOccupation());
			assertEquals(0, tb.secondaryBufferOccupation());
		}

	}

	class OverFlowListener implements BufferListener {

		TripleBuffer tb;

		public OverFlowListener(TripleBuffer tb) {
			super();
			this.tb = tb;
		}

		@Override
		public void bufferFull() {
//			System.out.println("BufferFull called");
			// Do shit
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.tb.clear();
		}

	}
}
