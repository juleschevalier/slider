package fr.ujm.tse.lt2c.satin.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import fr.ujm.tse.lt2c.satin.buffer.TripleBufferLock;
import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.triplestore.TripleImplNaive;

public class TripleBufferTester implements BufferListener{

	TripleBuffer buffer;
	long sum;
	static Random random;

	public TripleBufferTester() {
		this.buffer = new TripleBufferLock();
		this.buffer.addBufferListener(this);
		sum=0;
		random = new Random();
	}

	public void add(Triple triple){
		this.buffer.add(triple);
	}

	@Override
	public void bufferFull() {

		Thread t = new Thread(new Runnable() {
			public void run()
			{
				try {
					Thread.sleep(random.nextInt(2000));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				printSum(buffer.clear());
			}
		});
		t.start();

	}

	public void close(){

		Thread t = new Thread(new Runnable() {
			public void run()
			{
				try {
					Thread.sleep(random.nextInt(2000));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				printSum(buffer.close());
			}
		});
		t.start();
	}

	private void printSum(Collection<Triple> triples) {
		if(triples==null){
			System.out.println("NULL Collection");
			return;
		}
		long tempSum = 0;
		for (Triple triple : triples) {
			tempSum+=triple.getObject()+triple.getSubject()+triple.getPredicate();
			try {
				Thread.sleep(random.nextInt(20));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Partial sum ("+triples.size()+" elements) : "+tempSum);
		sum+=tempSum;
	}

	public long getSum() {
		return sum;
	}

	public static void main(String[] args) {

		TripleBufferTester tbt = new TripleBufferTester();
		Collection<Triple> triples = new ArrayList<>();


		for(int i=0; i<648; i++){
			Triple t = new TripleImplNaive(random.nextInt(100), random.nextInt(100), random.nextInt(100));
			triples.add(t);
			tbt.add(t);
		}
		tbt.close();
		System.out.println("This is the end. Let's check your stuff");

		long sum=0;
		for (Triple triple : triples) {
			sum+=triple.getObject()+triple.getSubject()+triple.getPredicate();
		}
		System.out.println("Real sum : "+sum);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Calculated sum : "+tbt.getSum());
		if(sum==tbt.getSum())
			System.out.println("Everything seems to be ok");
		else
			System.out.println("The sums are not egals");

	}

}
