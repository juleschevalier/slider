package fr.ujm.tse.lt2c.satin.rules.run;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.buffer.TripleBufferLock;
import fr.ujm.tse.lt2c.satin.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.RuleRun;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.reasoner.ReasonnerStreamed;

public abstract class AbstractRun implements RuleRun {

	private static Logger logger = Logger.getLogger(AbstractRun.class);

	protected Dictionary dictionary;
	protected TripleStore tripleStore;
	protected TripleDistributor distributor;
	protected TripleBuffer tripleBuffer;

	protected String ruleName = "";
	public static long[] input_matchers;
	public static long[] output_matchers;

	protected CountDownLatch doneSignal;
	protected boolean finished = false;
	protected int threads;

	public AbstractRun(Dictionary dictionary, TripleStore tripleStore, String ruleName, CountDownLatch doneSignal) {
		this.dictionary = dictionary;
		this.tripleStore = tripleStore;
		this.ruleName = ruleName;
		this.doneSignal = doneSignal;
		this.distributor = new TripleDistributor();
		this.tripleBuffer = new TripleBufferLock();
		this.threads = 0;

		this.distributor.setName(ruleName + " Distributor");
	}

	@Override
	public void run() {
		
		if(this.tripleBuffer.mainBufferOccupation()+this.tripleBuffer.secondaryBufferOccupation()==0){
			logger.warn("EMPTY");
			return;
		}

		if (logger.isTraceEnabled())
			logger.trace(this.ruleName + " START");
		ReasonnerStreamed.runningThreads.incrementAndGet();
		this.threads++;

		try {

			if (logger.isTraceEnabled())
				logger.trace(this.ruleName + " register on " + ReasonnerStreamed.phaser);
			ReasonnerStreamed.phaser.register();

			long DEBUG_loops = 0;

			/* Get triples from buffer */
			TripleStore usableTriples = this.tripleBuffer.clear();

			if (usableTriples == null)
				logger.warn(this.ruleName + " NULL usableTriples");

			Collection<Triple> outputTriples = new HashSet<>();

			if (usableTriples.isEmpty()) { // USELESS ??
				logger.warn(this.ruleName + " run without triples");
				// DEBUG_loops += process(tripleStore, tripleStore,
				// outputTriples);
			} else {
				DEBUG_loops += process(usableTriples, tripleStore, outputTriples);
				DEBUG_loops += process(tripleStore, usableTriples, outputTriples);
			}

			addNewTriples(outputTriples);

//			logDebug(this.ruleName + " : " + DEBUG_loops + " iterations for " + outputTriples.size() + " triples generated");
			if(logger.isDebugEnabled())
			logger.debug(this.ruleName + " : " + outputTriples.size() + " triples generated");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (logger.isTraceEnabled())
				logger.trace(this.ruleName + " arriveAndDeregister on " + ReasonnerStreamed.phaser);
			ReasonnerStreamed.phaser.arriveAndDeregister();
			ReasonnerStreamed.runningThreads.decrementAndGet();
			finish();
		}
	}

	abstract protected int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples);

	protected int addNewTriples(Collection<Triple> outputTriples) {
		int duplicates = 0;
		
		if(outputTriples.isEmpty())
			return duplicates;
		
		ArrayList<Triple> newTriples = new ArrayList<>();
		for (Triple triple : outputTriples) {
			if (!tripleStore.contains(triple)) {
				tripleStore.add(triple);
				newTriples.add(triple);
			} else {
				ReasonnerStreamed.DEBUG_nb_duplicates.incrementAndGet();
				logTrace(dictionary.printTriple(triple) + " already present");
			}
		}
		if (logger.isTraceEnabled())
			logger.trace(this.ruleName + " distribute " + newTriples.size() + " new triples");
		distributor.distribute(newTriples);
		return duplicates;
	}

	protected void logDebug(String message) {
		if (getLogger().isDebugEnabled()) {
			// getLogger().debug((usableTriples.isEmpty() ? "F " + ruleName +
			// " " : ruleName) + message);
			getLogger().debug(ruleName + " " + message);
		}
	}

	protected void logTrace(String message) {
		if (getLogger().isTraceEnabled()) {
			// getLogger().trace((usableTriples.isEmpty() ? "F " + ruleName +
			// " " : ruleName + " ") + message);
			getLogger().trace(ruleName + " " + message);
		}
	}

	protected void finish() {
		if (!this.finished) {
//			logTrace(" unlatching " + doneSignal.getCount());
//			this.finished = true;
//			doneSignal.countDown();
//			logTrace(" unlatched" + doneSignal.getCount());
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

	public TripleBuffer getTripleBuffer() {
		return this.tripleBuffer;
	}

	public TripleDistributor getDistributor() {
		return this.distributor;
	}

	abstract public long[] getInputMatchers();

	abstract public long[] getOutputMatchers();

	public int getThreads() {
		return threads;
	}

	@Override
	public String toString() {
		return this.ruleName;
	}

}
