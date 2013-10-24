import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import fr.ujm.tse.lt2c.satin.buffer.TripleBufferLock;
import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.triplestore.TripleImplNaive;

public class TestBuffer {

	public static void main(String[] args) {

		TripleBuffer buffer = new TripleBufferLock();
		ArrayList<Triple> triples = new ArrayList<>();

		BF bf = (new TestBuffer()).new BF(buffer);

		buffer.addBufferListener(bf);

		for (int i = 0; i < 1000; i++) {
			Random rnd = new Random();
			Triple t = new TripleImplNaive(rnd.nextLong(), rnd.nextLong(), rnd.nextLong());
			buffer.add(t);
			triples.add(t);
		}
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long sum = 0;
		for (Triple triple : triples) {
			sum += triple.getSubject();
			sum += triple.getPredicate();
			sum += triple.getObject();
		}
		System.out.println("Verif :");
		System.out.println("Total1 = " + sum);
		System.out.println("Total2 = " + bf.total_sum);

	}

	public class BF implements BufferListener {

		TripleBuffer tb;
		public long total_sum = 0;

		public BF(TripleBuffer tb) {
			this.tb = tb;
		}

		@Override
		public void bufferFull() {
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					count();

				}
			});
			t.start();
		}

		private void count() {
			Collection<Triple> triples = this.tb.clear();

			long sum = 0;
			for (Triple triple : triples) {
				sum += triple.getSubject();
				sum += triple.getPredicate();
				sum += triple.getObject();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			total_sum += sum;
			System.out.println("SUM: " + sum);
		}

	}

}
