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
	/*To remove*/
	String name="";
	
	public TripleDistributor() {
		super();
		this.subcribers = HashMultimap.create();
		this.universalSubscribers = new ArrayList<>();
	}
	
	public void subscribe(TripleBuffer tripleBuffer, long[] predicates){
		if(predicates.length==0){
			this.universalSubscribers.add(tripleBuffer);
			System.out.println("Here comes a new challenger "+name);
			return;
		}
		for (Long predicate : predicates) {
			this.subcribers.put(predicate, tripleBuffer);	
			System.out.println("Here comes a new challenger "+name);		
		}
	}
	
	public void distribute(Collection<Triple> triples){
		long distributed = 0;
		for (Triple triple : triples) {
			long p = triple.getPredicate();
			for (TripleBuffer tripleBuffer : this.subcribers.get(p)) {
				while(!tripleBuffer.add(triple));
			}
			for (TripleBuffer tripleBuffer : this.universalSubscribers) {
				while(!tripleBuffer.add(triple));
			}
			distributed+=(this.universalSubscribers.size()+this.subcribers.get(p).size());
		}
		System.out.println(distributed+" distributed on "+triples.size()+" ("+(this.subcribers.size()+this.universalSubscribers.size())+" subscribers)");
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String subcribers(){
		return ""+(this.subcribers.size()+this.universalSubscribers.size());
	}

}
