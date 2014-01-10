package fr.ujm.tse.lt2c.satin.buffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.rules.Rule;

/**
 * @author Jules Chevalier
 * 
 *         This class is in charge of linking all the buffers together
 * 
 */
public class TripleManager {

	private static Logger logger = Logger.getLogger(TripleManager.class);

	ArrayList<Rule> rules;
	Multimap<String, String> DEBUG_links;
	TripleDistributor generalDistributor;

	static final long TIMEOUT = 100; // Timeout

	public TripleManager() {
		super();
		this.rules = new ArrayList<>();
		DEBUG_links = HashMultimap.create();
		this.generalDistributor = new TripleDistributor();
		this.generalDistributor.setName("T_MANAGER");
		// timeoutChecker();
	}

	public void addRule(Rule newRule) {
		this.rules.add(newRule);
		generalDistributor.addSubscriber(newRule.getTripleBuffer(), newRule.getInputMatchers());
		if (logger.isTraceEnabled()) {
			logger.trace("Triple Manager : -- ADD RULE " + newRule.name() + " --");
			logger.trace("Triple Manager : General -> " + newRule.name());
		}
		for (Rule rule : this.rules) {
			if (match(newRule.getOutputMatchers(), rule.getInputMatchers())) {
				long[] matchers = extractMatchers(newRule.getOutputMatchers(), rule.getInputMatchers());
				newRule.getTripleDistributor().addSubscriber(rule.getTripleBuffer(), matchers);
				DEBUG_links.put(newRule.name(), rule.name());
				if (logger.isTraceEnabled())
					logger.trace("Triple Manager : " + rule.name() + " -> " + newRule.name());
			}
			if ((rule != newRule) && match(rule.getOutputMatchers(), newRule.getInputMatchers())) {
				rule.getTripleDistributor().addSubscriber(newRule.getTripleBuffer(), newRule.getInputMatchers());
				DEBUG_links.put(rule.name(), newRule.name());
				if (logger.isTraceEnabled())
					logger.trace("Triple Manager : " + newRule.name() + " -> " + rule.name());
			}
		}
	}

	public void addTriples(Collection<Triple> triples) {
		this.generalDistributor.distribute(triples);
	}

	public long finishThem() {
		long total = 0;
		for (Rule rule : this.rules) {
			if ((rule.getTripleBuffer().mainBufferOccupation() + rule.getTripleBuffer().secondaryBufferOccupation()) > 0)
				total++;
			if (logger.isTraceEnabled())
				logger.trace(rule.name() + " buffer : " + rule.getTripleBuffer().mainBufferOccupation() + "," + rule.getTripleBuffer().secondaryBufferOccupation());
			rule.bufferFull();
		}
		return total;

	}

	public Collection<Rule> getRules() {
		return this.rules;
	}

	private boolean match(long[] in, long[] out) {
		if (in.length == 0 || out.length == 0)
			return true;
		// Broken loops
		for (long l1 : out) {
			for (long l2 : in) {
				if (l1 == l2)
					return true;
			}
		}
		return false;
	}

	private long[] extractMatchers(long[] out, long[] in) {
		if (in.length == 0 || out.length == 0)
			return new long[] {};

		List<Long> matchers = new ArrayList<Long>();
		for (long l1 : out) {
			for (long l2 : in) {
				if (l1 == l2)
					matchers.add(l1);
			}
		}

		long[] ms = new long[matchers.size()];

		for (int i = 0; i < matchers.size(); i++) {
			ms[i] = matchers.get(i);
		}

		return ms;
	}

	private void timeoutChecker() {
		Runnable checker = new Runnable() {
			public void run() {
				try {
					Thread.sleep(10);
					for (Rule rule : rules) {
						if ((rule.getTripleBuffer().getLastFlush() - System.nanoTime()) > (TIMEOUT * 1000000))
							rule.getTripleBuffer().sendFullBuffer();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		Executors.newSingleThreadExecutor().submit(checker);
	}

}
