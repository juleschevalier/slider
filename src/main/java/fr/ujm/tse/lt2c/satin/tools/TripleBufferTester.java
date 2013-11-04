package fr.ujm.tse.lt2c.satin.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import fr.ujm.tse.lt2c.satin.buffer.TripleBufferLock;
import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;

public class TripleBufferTester implements BufferListener {

	TripleBuffer tripleBuffer;
	static Random random;
	ExecutorService executor;
	static Integer submittedThreads = 0;
	AtomicLong sum = new AtomicLong(0);

	public TripleBufferTester() {
		this.tripleBuffer = new TripleBufferLock();
		this.tripleBuffer.addBufferListener(this);
		this.executor = Executors.newCachedThreadPool();
		random = new Random();
	}

	public void add(Triple triple) {
		sum.incrementAndGet();
		this.tripleBuffer.add(triple);
	}

	@Override
	public void bufferFull() {

		Thread t = new Thread(new Runnable() {
			public void run() {
				TripleStore ts = tripleBuffer.clear();
				try {
					Thread.sleep(random.nextInt(200));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//printSum(ts);
				submittedThreads--;
			}
		});
		this.executor.submit(t);
		submittedThreads++;

	}

	public void close() {

		Thread t = new Thread(new Runnable() {
			public void run() {
				TripleStore ts = tripleBuffer.close();
				try {
					Thread.sleep(random.nextInt(200));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//printSum(ts);
				submittedThreads--;
			}
		});
		this.executor.submit(t);
		submittedThreads++;
	}
/*
	private void printSum(TripleStore triples) {
		if (triples == null) {
			System.out.println("NULL Collection");
			return;
		}
		long tempSum = 0;
		for (Triple triple : triples.getAll()) {
			tempSum++;// = triple.getObject() + triple.getSubject() +
						// triple.getPredicate();
			try {
				Thread.sleep(random.nextInt(20));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// System.out.println("Partial sum (" + triples.size() + " elements) : "
		// + tempSum);
		sum += tempSum;
	}*/
/*
	public long getSum() {
		return sum;
	}*/

	public static void main(String[] args) {

		int nb_errors=0;
		int nb_tests=100;
		for (int i = 0; i < nb_tests; i++) {
			if(!test())
				nb_errors++;
		}
		System.out.println("-------------------------------------------------------------------------");
		System.out.println(nb_errors+" error"+(nb_errors>1?"s":"")+" for "+nb_tests+" tests");

	}

	private static boolean test() {

		int LOOPS = 568;

		TripleBufferTester tbt = new TripleBufferTester();
		Collection<Triple> triples = new ArrayList<>();

		submittedThreads = 0;

		for (int i = 0; i < LOOPS; i++) {
			Triple t = new ImmutableTriple(random.nextInt(100), random.nextInt(100), random.nextInt(100));
			triples.add(t);
			tbt.add(t);
		}
		tbt.close();
		tbt.executor.shutdown();

//		System.out.println("Left threads : " + submittedThreads);

		long sum = 0;
		sum += triples.size();
		// System.out.println("Real sum : " + sum);
		try {
			tbt.executor.awaitTermination(1, TimeUnit.DAYS);
			// Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		System.out.println("Left threads : " + submittedThreads);
		// System.out.println("Calculated sum : " + tbt.getSum());
		boolean ok = true;
		System.out.println("-------------------------------------------------------------------------");
		if (sum != tbt.getSum()){
			ok=false;
			System.out.println("The sums are not egals (" + sum + " and " + tbt.getSum() + ")");
		}
		if(submittedThreads!=0){
			ok=false;
			System.out.println("There are some missing threads ("+submittedThreads+")");
		}
		if(ok){
			System.out.println("Everything seems to be ok");
		}
		
		return ok;
	}

	private long getSum() {
		return sum.intValue();
	}
}
