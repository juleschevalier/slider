package fr.ujm.tse.lt2c.satin.triplestore;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashSet;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
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
			if (t.getSubject() == s) {
				result.add(t);
			}
		}
		return result;
	}

	@Override
	public Collection<Triple> getbyPredicate(long p) {
		HashSet<Triple> result = new HashSet<>();
		for (Triple t : this.triples) {
			if (t.getPredicate() == p) {
				result.add(t);
			}
		}
		return result;
	}

	@Override
	public Collection<Triple> getbyObject(long o) {
		HashSet<Triple> result = new HashSet<>();
		for (Triple t : this.triples) {
			if (t.getObject() == o) {
				result.add(t);
			}
		}
		return result;
	}

	@Override
	public long size() {
		return triples.size();
	}

	@Override
	public boolean isEmpty() {
		return triples.isEmpty();
	}

	@Override
	public void writeToFile(String file, Dictionnary dictionnary) {

		try {
			// Create file
			FileWriter fstream = new FileWriter(file, false);
			BufferedWriter out = new BufferedWriter(fstream);
			for (Triple triple : this.triples) {
				out.write(dictionnary.printTriple(triple) + "\n");
			}
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
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

	@Override
	public boolean contains(Triple triple) {

		return triples.contains(triple);
	}
}
