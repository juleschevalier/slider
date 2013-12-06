package fr.ujm.tse.lt2c.satin.buffer;

import java.util.ArrayList;
import java.util.Collection;

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

	ArrayList<Rule> rules;
	Multimap<String, String> links;/*Useless finaly*/
	TripleDistributor generalDistributor;

	public TripleManager() {
		super();
		this.rules = new ArrayList<>();
		links = HashMultimap.create();
		this.generalDistributor = new TripleDistributor();
	}

	public void addRule(Rule newRule) {
		this.rules.add(newRule);
		generalDistributor.subscribe(newRule.getTripleBuffer(), newRule.getInputMatchers());
		for (Rule rule : this.rules) {
			// System.out.print(".");
			if (match(newRule.getOutputMatchers(), rule.getInputMatchers())) {
				// System.out.println("MATCH !");
				newRule.getTripleDistributor().subscribe(rule.getTripleBuffer(), rule.getInputMatchers());
				links.put(newRule.name(), rule.name());
//				 System.out.println(rule.name()+"->"+newRule.name());
			}
			if (match(rule.getOutputMatchers(), newRule.getInputMatchers())) {
				// System.out.println("MATCH !");
				rule.getTripleDistributor().subscribe(newRule.getTripleBuffer(), newRule.getInputMatchers());
				links.put(rule.name(), newRule.name());
//				 System.out.println(newRule.name()+"->"+rule.name());
			}

		}
		// System.out.print(newRule.name()+" {");
		// for (long in : newRule.getInputMatchers()) {
		// System.out.print(in+" ");
		// }
		// System.out.print("} {");
		// for (long out : newRule.getOutputMatchers()) {
		// System.out.print(out+" ");
		// }
		// System.out.println("}");
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
