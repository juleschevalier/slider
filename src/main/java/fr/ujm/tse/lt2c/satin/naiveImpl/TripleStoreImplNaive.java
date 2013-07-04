package fr.ujm.tse.lt2c.satin.naiveImpl;

import java.util.Collection;
import java.util.HashSet;

import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;

public class TripleStoreImplNaive implements TripleStore {
	
	HashSet<Triple> triples;
	
	public TripleStoreImplNaive() {
		super();
		this.triples = new HashSet<>();
	}

	@Override
	public void add(Triple t) {
		this.triples.add(t);
	}

	@Override
	public void addAll(Collection<Triple> t) {
		this.triples.addAll(t);
	}

	@Override
	public Collection<Triple> getAll() {
		return this.triples;
	}

	@Override
	public Collection<Triple> getbySubject(long s) {
		HashSet<Triple> result = new HashSet<>();
		for (Triple t : this.triples) {
			if(t.getSubject()==s){
				result.add(t);
			}
		}
		return result;
	}

	@Override
	public Collection<Triple> getbyPredicate(long p) {
		HashSet<Triple> result = new HashSet<>();
		for (Triple t : this.triples) {
			if(t.getPredicate()==p){
				result.add(t);
			}
		}
		return result;
	}

	@Override
	public Collection<Triple> getbyObject(long o) {
		HashSet<Triple> result = new HashSet<>();
		for (Triple t : this.triples) {
			if(t.getObject()==o){
				result.add(t);
			}
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((triples == null) ? 0 : triples.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TripleStoreImplNaive other = (TripleStoreImplNaive) obj;
		if (triples == null) {
			if (other.triples != null)
				return false;
		} else if (!triples.equals(other.triples))
			return false;
		return true;
	}

}
