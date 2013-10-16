package fr.ujm.tse.lt2c.satin.rules;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.reasoner.ReasonnerVerticalMTRWLock;

public abstract class AbstractRule implements Rule {

	protected Dictionary dictionary;
	protected TripleStore tripleStore;
	protected TripleStore usableTriples;
	protected Collection<Triple> newTriples;
	protected String ruleName = "";
	protected CountDownLatch doneSignal;
	protected boolean finished = false;

	public AbstractRule(Dictionary dictionary, TripleStore tripleStore,
			TripleStore usableTriples, Collection<Triple> newTriples,
			String ruleName, CountDownLatch doneSignal) {
		this.dictionary = dictionary;
		this.tripleStore = tripleStore;
		this.usableTriples = usableTriples;
		this.newTriples = newTriples;
		this.ruleName = ruleName;
		this.doneSignal = doneSignal;
	}

	@Override
	public void run() {

		try {

			long loops = 0;

			Collection<Triple> outputTriples = new HashSet<>();

			if (usableTriples.isEmpty()) {
				loops += process(tripleStore, tripleStore, outputTriples);
			} else {
				loops += process(usableTriples, tripleStore, outputTriples);
				loops += process(tripleStore, usableTriples, outputTriples);
			}

			addNewTriples(outputTriples);

			logDebug(this.getClass() + " : " + loops
					+ " iterations - outputTriples  " + outputTriples.size());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			finish();

		}
	}

	abstract protected int process(TripleStore ts1, TripleStore ts2,
			Collection<Triple> outputTriples);

	protected int addNewTriples(Collection<Triple> outputTriples) {
		int duplicates = 0;
		// ReasonnerVerticalMTRWLock.cdlWriter.countDown();
		// try {
		// ReasonnerVerticalMTRWLock.cdlWriter.await();
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		for (Triple triple : outputTriples) {
			if (!tripleStore.contains(triple)) {
				tripleStore.add(triple);
				newTriples.add(triple);
			} else {
				ReasonnerVerticalMTRWLock.nb_duplicates.incrementAndGet();
				logTrace(dictionary.printTriple(triple) + " already present");
			}
		}
		return duplicates;
	}

	protected void logDebug(String message) {
		if (getLogger().isDebugEnabled()) {
			getLogger()
					.debug((usableTriples.isEmpty() ? "F " + ruleName + " "
							: ruleName) + message);
		}
	}

	protected void logTrace(String message) {
		if (getLogger().isTraceEnabled()) {
			getLogger().trace(
					(usableTriples.isEmpty() ? "F " + ruleName + " " : ruleName
							+ " ")
							+ message);
		}
	}

	protected void finish() {
		if (!this.finished) {
			logDebug(" unlatching " + doneSignal.getCount());
			this.finished = true;
			doneSignal.countDown();
			logDebug(" unlatched" + doneSignal.getCount());
		}
	}

	public CountDownLatch getDoneSignal() {
		return doneSignal;
	}

	public void setDoneSignal(CountDownLatch doneSignal) {
		this.doneSignal = doneSignal;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public String getRuleName() {
		return ruleName;
	}

}
