package fr.ujm.tse.lt2c.satin.buffer;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;

public class TripleDistributor {
	
	private Multimap<Long, TripleBuffer> subcribers;
	private Collection<TripleBuffer> universalSubscribers;
	
	public TripleDistributor() {
		super();
		this.subcribers = HashMultimap.create();
		this.universalSubscribers = new ArrayList<>();
	}
	
	public void subscribe(TripleBuffer tripleBuffer, long[] predicates){
		if(predicates.length==0){
			this.universalSubscribers.add(tripleBuffer);
			return;
		}
		for (Long predicate : predicates) {
			this.subcribers.put(predicate, tripleBuffer);			
		}
	}
	
	public void distribute(Collection<Triple> triples){
		for (Triple triple : triples) {
			long p = triple.getPredicate();
			for (TripleBuffer tripleBuffer : this.subcribers.get(p)) {
				tripleBuffer.add(triple);
			}
			for (TripleBuffer tripleBuffer : this.universalSubscribers) {
				tripleBuffer.add(triple);
			}
		}
	}

}
