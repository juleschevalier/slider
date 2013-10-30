package fr.ujm.tse.lt2c.satin.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import fr.ujm.tse.lt2c.satin.buffer.TripleBufferLock;
import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.TripleImplNaive;

public class TripleBufferTester implements BufferListener {

	TripleBuffer tripleBuffer;
	long sum;
	static Random random;
	ExecutorService executor;

	public TripleBufferTester() {
		this.tripleBuffer = new TripleBufferLock();
		this.tripleBuffer.addBufferListener(this);
		this.executor = Executors.newCachedThreadPool();
		sum = 0;
		random = new Random();
	}

	public void add(Triple triple) {
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
				printSum(ts);
			}
		});
		this.executor.submit(t);

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
				printSum(ts);
			}
		});
		this.executor.submit(t);
	}

	private void printSum(TripleStore triples) {
		if (triples == null) {
			System.out.println("NULL Collection");
			return;
		}
		long tempSum = 0;
		for (Triple triple : triples.getAll()) {
			tempSum ++;//= triple.getObject() + triple.getSubject() + triple.getPredicate();
			try {
				Thread.sleep(random.nextInt(20));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
//		System.out.println("Partial sum (" + triples.size() + " elements) : " + tempSum);
		sum += tempSum;
	}

	public long getSum() {
		return sum;
	}

	public static void main(String[] args) {

		for (int i = 0; i < 50; i++) {
			test();
		}

	}

	private static void test() {
		TripleBufferTester tbt = new TripleBufferTester();
		Collection<Triple> triples = new ArrayList<>();

		for (int i = 0; i < 864; i++) {
			Triple t = new TripleImplNaive(random.nextInt(100), random.nextInt(100), random.nextInt(100));
			triples.add(t);
			tbt.add(t);
		}
		tbt.close();
		tbt.executor.shutdown();

		long sum = 0;
		sum+=triples.size();
//		System.out.println("Real sum : " + sum);
		try {
			tbt.executor.awaitTermination(1, TimeUnit.DAYS);
			// Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		System.out.println("Calculated sum : " + tbt.getSum());
		if (sum == tbt.getSum())
			System.out.println("Everything seems to be ok");
		else
			System.out.println("The sums are not egals ("+sum+" and "+tbt.getSum()+")");
	}

}
