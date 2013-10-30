package fr.ujm.tse.lt2c.satin.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

import fr.ujm.tse.lt2c.satin.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.reasoner.ReasonnerVerticalMTRWLock;

public abstract class AbstractRule implements Rule {

	protected Dictionary dictionary;
	protected TripleStore tripleStore;
	protected TripleDistributor distributor;
	protected TripleBuffer tripleBuffer;
	protected String ruleName = "";
	public static long[] matchers;

	protected CountDownLatch doneSignal;
	protected boolean finished = false;

	public AbstractRule(Dictionary dictionary, TripleStore tripleStore, String ruleName, CountDownLatch doneSignal, TripleDistributor distributor, TripleBuffer tripleBuffer) {
		this.dictionary = dictionary;
		this.tripleStore = tripleStore;
		this.ruleName = ruleName;
		this.doneSignal = doneSignal;
		this.distributor = distributor;
		this.tripleBuffer = tripleBuffer;
	}

	@Override
	public void run() {

		try {

			long loops = 0;
			
			TripleStore usableTriples = tripleBuffer.clear();

			Collection<Triple> outputTriples = new HashSet<>();

			if (usableTriples.isEmpty()) {
				loops += process(tripleStore, tripleStore, outputTriples);
			} else {
				loops += process(usableTriples, tripleStore, outputTriples);
				loops += process(tripleStore, usableTriples, outputTriples);
			}

			addNewTriples(outputTriples);

			logDebug(this.getClass() + " : " + loops + " iterations - outputTriples  " + outputTriples.size());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			finish();
		}
	}

	abstract protected int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples);

	protected int addNewTriples(Collection<Triple> outputTriples) {
		int duplicates = 0;
		ArrayList<Triple> newTriples = new ArrayList<>();
		for (Triple triple : outputTriples) {
			if (!tripleStore.contains(triple)) {
				tripleStore.add(triple);
				newTriples.add(triple);
			} else {
				ReasonnerVerticalMTRWLock.nb_duplicates.incrementAndGet();
				logTrace(dictionary.printTriple(triple) + " already present");
			}
		}
		distributor.distribute(newTriples);
		return duplicates;
	}

	protected void logDebug(String message) {
		if (getLogger().isDebugEnabled()) {
//			getLogger().debug((usableTriples.isEmpty() ? "F " + ruleName + " " : ruleName) + message);
			getLogger().debug(ruleName + " " + message);
		}
	}

	protected void logTrace(String message) {
		if (getLogger().isTraceEnabled()) {
//			getLogger().trace((usableTriples.isEmpty() ? "F " + ruleName + " " : ruleName + " ") + message);
			getLogger().trace(ruleName + " " + message);
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
