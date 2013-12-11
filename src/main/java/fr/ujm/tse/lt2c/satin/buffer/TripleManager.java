package fr.ujm.tse.lt2c.satin.buffer;

import java.util.ArrayList;
import java.util.Collection;

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

	public TripleManager() {
		super();
		this.rules = new ArrayList<>();
		DEBUG_links = HashMultimap.create();
		this.generalDistributor = new TripleDistributor();
	}

	public void addRule(Rule newRule) {
		this.rules.add(newRule);
		generalDistributor.subscribe(newRule.getTripleBuffer(), newRule.getInputMatchers());
		for (Rule rule : this.rules) {
			if (match(newRule.getOutputMatchers(), rule.getInputMatchers())) {
				newRule.getTripleDistributor().subscribe(rule.getTripleBuffer(), rule.getInputMatchers());
				DEBUG_links.put(newRule.name(), rule.name());
				logger.trace("Triple Manager : " + rule.name() + "->" + newRule.name());
			}
			if (match(rule.getOutputMatchers(), newRule.getInputMatchers())) {
				rule.getTripleDistributor().subscribe(newRule.getTripleBuffer(), newRule.getInputMatchers());
				DEBUG_links.put(rule.name(), newRule.name());
				logger.trace("Triple Manager : " + newRule.name() + "->" + rule.name());
			}

		}
	}

	public void addTriples(Collection<Triple> triples) {
		this.generalDistributor.distribute(triples);
	}

	public void finishThem() {
		for (Rule rule : this.rules) {
			if (rule.getTripleBuffer().mainBufferOccupation() > 0)
				rule.bufferFull();
		}
	}

	public Collection<Rule> getRules() {
		return this.rules;
	}

	private boolean match(long[] in, long[] out) {
		if (in.length == 0 || out.length == 0)
			return true;
		// Broken loops
		for (long l : out) {
			for (long l2 : in) {
				if (l == l2)
					return true;
			}
		}
		return false;
	}

}
