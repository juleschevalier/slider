package fr.ujm.tse.lt2c.satin.naiveImpl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;


/**
 * @author Jules Chevalier
 *
 */
public class DictionnaryImplNaive implements Dictionnary {

	private HashMap<String, Long> triples;
	long counter;

	public DictionnaryImplNaive() {
		super();
		this.triples = new HashMap<>();
		this.counter = 0;
	}

	@Override
	public long add(String s) {
		if(this.triples.containsKey(s)){
			return this.get(s);
		}
		this.triples.put(s, this.counter);
		return this.counter++;
	}

	@Override
	public String get(long index) {
		Iterator<Entry<String, Long>> it = this.triples.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Long> pairs = (Entry<String, Long>) it.next();
			if (pairs.getValue().equals(index))
				return pairs.getKey();
		}
		return null;
	}

	@Override
	public long get(String s) {
		return this.triples.get(s);
	}

	@Override
	public long size() {
		return this.triples.size();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (counter ^ (counter >>> 32));
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
		DictionnaryImplNaive other = (DictionnaryImplNaive) obj;
		if (counter != other.counter)
			return false;
		if (triples == null) {
			if (other.triples != null)
				return false;
		} else if (!triples.equals(other.triples))
			return false;
		return true;
	}
	
	@Override
	public String printTriple(Triple t){
		String s = this.get(t.getSubject()),
			   p = this.get(t.getPredicate()),
			   o = this.get(t.getObject());
		
		if(s.split("#").length>1)
			s=s.split("#")[1];
		if(p.split("#").length>1)
			p=p.split("#")[1];
		if(o.split("#").length>1)
			o=o.split("#")[1];
		
		return s+" "+p+" "+o;
	}

}
