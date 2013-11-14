package fr.ujm.tse.lt2c.satin.triplebuffer.test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import fr.ujm.tse.lt2c.satin.buffer.TripleBufferLock;
import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;
import static org.junit.Assert.assertEquals;

public class TestMultiThreadInsertTripleBuffer {

	public final static int PROBA = 1000000;
	public static final int THREADS = 50;

	@Test
	public void test() {
		for (int k = 0; k < 150; k++) {
			TripleBuffer tb = new TripleBufferLock();
			SimpleBufferListener listener = new SimpleBufferListener(tb);
			tb.addBufferListener(listener);
			Set<Triple> generated = Collections.synchronizedSet(new HashSet<Triple>());

			// Test buffer flush
			ExecutorService executor = Executors.newCachedThreadPool();
			int maxvalue = 45000;
			for (int j = 0; j < THREADS; j++) {
				executor.submit(new RunnableAdder(generated, tb, maxvalue));
			}
			executor.shutdown();
			try {
				executor.awaitTermination(1, TimeUnit.DAYS);
				// Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// Assert set equality
			long listenerf = listener.finish();
			// System.out.println(listenerf);
			assertEquals(generated.size(), listenerf);

		}
	}

	class RunnableAdder implements Runnable {

		Set<Triple> generated;
		TripleBuffer tb;
		int max;

		public RunnableAdder(Set<Triple> generated, TripleBuffer tb, int max) {
			super();
			this.generated = generated;
			this.tb = tb;
			this.max = max;
		}

		@Override
		public void run() {
			Random random = new Random();
			while (generated.size() < max) {
				Triple newTriple = new ImmutableTriple(random.nextInt(PROBA), random.nextInt(PROBA), random.nextInt(PROBA));
				generated.add(newTriple);
				while (!tb.add(newTriple))
					;
			}
		}

	}

	class SimpleBufferListener implements BufferListener {

		TripleBuffer tb;
		long counter;

		public SimpleBufferListener(TripleBuffer tb) {
			super();
			this.tb = tb;
			counter = 0;
		}

		@Override
		public void bufferFull() {
			assertEquals(0, tb.mainBufferOccupation());
			assertEquals(tb.getBufferLimit(), tb.secondaryBufferOccupation());
			TripleStore emptyedBuffer = tb.clear();
			assertEquals(emptyedBuffer.size(), tb.getBufferLimit());
			// TODO possible shit here
			assertEquals(0, tb.secondaryBufferOccupation());
			counter += emptyedBuffer.size();
		}

		public long finish() {
			return counter + tb.flush().size();
		}

	}

}
