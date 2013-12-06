package fr.ujm.tse.lt2c.satin.rules.run;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

import fr.ujm.tse.lt2c.satin.buffer.TripleBufferLock;
import fr.ujm.tse.lt2c.satin.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.RuleRun;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.reasoner.ReasonnerStreamed;

public abstract class AbstractRun implements RuleRun {

	protected Dictionary dictionary;
	protected TripleStore tripleStore;
	protected TripleDistributor distributor;

	protected TripleBuffer tripleBuffer;
	protected String ruleName = "";
	public static long[] input_matchers;
	public static long[] output_matchers;

	protected CountDownLatch doneSignal;
	protected boolean finished = false;

	public AbstractRun(Dictionary dictionary, TripleStore tripleStore, String ruleName, CountDownLatch doneSignal) {
		this.dictionary = dictionary;
		this.tripleStore = tripleStore;
		this.ruleName = ruleName;
		this.doneSignal = doneSignal;
		this.distributor = new TripleDistributor();
		this.tripleBuffer = new TripleBufferLock();
	}

	@Override
	public void run() {

		// System.out.println("THIS IS A RUN");
		ReasonnerStreamed.runningThreads.incrementAndGet();

		try {

			long loops = 0;

			// System.out.println(ruleName+" BEFORE CLEAR : "+this.tripleBuffer.mainBufferOccupation()+" "+this.tripleBuffer.secondaryBufferOccupation());
			TripleStore usableTriples = this.tripleBuffer.clear();
			// System.out.println(ruleName+" AFTER  CLEAR : "+this.tripleBuffer.mainBufferOccupation()+" "+this.tripleBuffer.secondaryBufferOccupation());

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
			ReasonnerStreamed.runningThreads.decrementAndGet();
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
				ReasonnerStreamed.nb_duplicates.incrementAndGet();
				logTrace(dictionary.printTriple(triple) + " already present");
			}
		}
		System.out.println("Distribute "+newTriples.size());
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

	public TripleBuffer getTripleBuffer() {
		return this.tripleBuffer;
	}

	public TripleDistributor getDistributor() {
		return this.distributor;
	}

	abstract public long[] getInputMatchers();

	abstract public long[] getOutputMatchers();

	@Override
	public String toString() {
		return this.ruleName;
	}

	// public static AbstractRun getNewInstance(Class runType, Dictionary
	// dictionary, TripleStore tripleStore, CountDownLatch doneSignal){
	// if(runType.equals(RunCAX_SCO.class)){
	// return new RunCAX_SCO(dictionary, tripleStore, doneSignal);
	// }
	// if(runType.equals(RunPRP_DOM.class)){
	// return new RunPRP_DOM(dictionary, tripleStore, doneSignal);
	// }
	// if(runType.equals(RunPRP_RNG.class)){
	// return new RunPRP_RNG(dictionary, tripleStore, doneSignal);
	// }
	// if(runType.equals(RunPRP_SPO1.class)){
	// return new RunPRP_SPO1(dictionary, tripleStore, doneSignal);
	// }
	// if(runType.equals(RunSCM_DOM1.class)){
	// return new RunSCM_DOM1(dictionary, tripleStore, doneSignal);
	// }
	// if(runType.equals(RunSCM_DOM2.class)){
	// return new RunSCM_DOM2(dictionary, tripleStore, doneSignal);
	// }
	// if(runType.equals(RunSCM_EQC2.class)){
	// return new RunSCM_EQC2(dictionary, tripleStore, doneSignal);
	// }
	// if(runType.equals(RunSCM_EQP2.class)){
	// return new RunSCM_EQP2(dictionary, tripleStore, doneSignal);
	// }
	// if(runType.equals(RunSCM_RNG1.class)){
	// return new RunSCM_RNG1(dictionary, tripleStore, doneSignal);
	// }
	// if(runType.equals(RunSCM_RNG2.class)){
	// return new RunSCM_RNG2(dictionary, tripleStore, doneSignal);
	// }
	// if(runType.equals(RunSCM_SCO.class)){
	// return new RunSCM_SCO(dictionary, tripleStore, doneSignal);
	// }
	// if(runType.equals(RunSCM_SPO.class)){
	// return new RunSCM_SPO(dictionary, tripleStore, doneSignal);
	// }
	//
	// return null;
	// }

}
