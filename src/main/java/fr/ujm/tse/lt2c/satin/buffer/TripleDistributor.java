package fr.ujm.tse.lt2c.satin.buffer;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;

public class TripleDistributor {

	private static Logger logger = Logger.getLogger(TripleDistributor.class);

	private Multimap<Long, TripleBuffer> subcribers;
	private Collection<TripleBuffer> universalSubscribers;
	String DEBUG_name = "";

	public TripleDistributor() {
		super();
		this.subcribers = HashMultimap.create();
		this.universalSubscribers = new ArrayList<>();
	}

	public void subscribe(TripleBuffer tripleBuffer, long[] predicates) {
		if (predicates.length == 0) {
			this.universalSubscribers.add(tripleBuffer);
			return;
		}
		for (Long predicate : predicates) {
			this.subcribers.put(predicate, tripleBuffer);
		}
	}

	public void distribute(Collection<Triple> triples) {
		long DEBUG_distributed = 0;
		for (Triple triple : triples) {
			long p = triple.getPredicate();
			for (TripleBuffer tripleBuffer : this.subcribers.get(p)) {
				while (!tripleBuffer.add(triple))
					;
			}
			for (TripleBuffer tripleBuffer : this.universalSubscribers) {
				while (!tripleBuffer.add(triple))
					;
			}
			DEBUG_distributed += (this.universalSubscribers.size() + this.subcribers.get(p).size());
		}
		logger.trace(DEBUG_distributed + " distributed on " + triples.size() + " (" + (this.subcribers.size() + this.universalSubscribers.size()) + " subscribers)");
	}

	public void setName(String name) {
		this.DEBUG_name = name;
	}

//	public String subcribers() {
//		return "" + (this.subcribers.size() + this.universalSubscribers.size());
//	}

}
