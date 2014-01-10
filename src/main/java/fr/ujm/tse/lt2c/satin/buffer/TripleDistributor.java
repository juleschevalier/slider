package fr.ujm.tse.lt2c.satin.buffer;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;

public class TripleDistributor {

	private static Logger logger = Logger.getLogger(TripleDistributor.class);

	private Multimap<Long, TripleBuffer> subscribers;
	private Collection<TripleBuffer> universalSubscribers;
	String DEBUG_name = "";

	public TripleDistributor() {
		super();
		this.subscribers = HashMultimap.create();
		this.universalSubscribers = new HashSet<>();
	}

	public void addSubscriber(TripleBuffer tripleBuffer, long[] predicates) {
		if (predicates.length == 0) {
			if (!this.universalSubscribers.contains(tripleBuffer))
				this.universalSubscribers.add(tripleBuffer);
			return;
		}
		for (Long predicate : predicates) {
			if (!this.subscribers.containsEntry(predicate, tripleBuffer))
				this.subscribers.put(predicate, tripleBuffer);
		}
	}

	public void distribute(Collection<Triple> triples) {
		/*
		 * ISSUE -> ALL BUFFERS WAIT FOR ONE BLOCKED
		 */
		long DEBUG_distributed = 0;
		for (Triple triple : triples) {
			long p = triple.getPredicate();
			for (TripleBuffer tripleBuffer : this.subscribers.get(p)) {
				while (!tripleBuffer.add(triple))
					;
			}
			for (TripleBuffer tripleBuffer : this.universalSubscribers) {
				while (!tripleBuffer.add(triple))
					;
			}
			DEBUG_distributed += (this.universalSubscribers.size() + this.subscribers.get(p).size());
		}
		if (logger.isTraceEnabled())
			logger.trace(DEBUG_name + " " + DEBUG_distributed + " triples sent (" + triples.size() + " unique triples, " + (this.subscribers.size() + this.universalSubscribers.size()) + " subscribers)");
	}

	public void setName(String name) {
		this.DEBUG_name = name;
	}

	public int subscribersNumber() {
		return this.subscribers.values().size() + this.universalSubscribers.size();
	}

	public String subscribers(String name, Dictionary dictionary) {
		StringBuilder subs = new StringBuilder();
		subs.append("\n");
		for (TripleBuffer buffer : universalSubscribers) {
			subs.append(name + " send to " + buffer.getDEBUG_name() + " for *\n");
		}
		for (Long predicate : subscribers.keySet()) {
			for (TripleBuffer buffer : subscribers.get(predicate)) {
				subs.append(name + " send to " + buffer.getDEBUG_name() + " for " + dictionary.printConcept(dictionary.get(predicate)) + "\n");
			}
		}
		return subs.toString();
	}

}
