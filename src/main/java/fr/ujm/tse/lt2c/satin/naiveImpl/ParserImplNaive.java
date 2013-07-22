package fr.ujm.tse.lt2c.satin.naiveImpl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Triple;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;

public class ParserImplNaive implements Parser {
	
	private static Logger logger = Logger.getLogger(ParserImplNaive.class);
	
	Dictionnary dictionnary;
	TripleStore tripleStore;

	/**
	 * @param f the file to parse
	 */
	public ParserImplNaive(Dictionnary dictionnary, TripleStore tripleStore) {
		this.dictionnary = dictionnary;
		this.tripleStore = tripleStore;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.ujm.tse.lt2c.satin.Parser#parse()
	 */
	@Override
	public void parse(final String fileInput) {
		PipedRDFIterator<Triple> iter = new PipedRDFIterator<Triple>();
		final PipedRDFStream<Triple> inputStream = new PipedTriplesStream(iter);

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Runnable parser = new Runnable() {

			public void run() {
				// Call the parsing process.
				RDFDataMgr.parse(inputStream, fileInput);
			}
		};

		executor.submit(parser);
		while (iter.hasNext()) {
			Triple next = iter.next();
			addTriple(next);
		}
	}

	private void addTriple(Triple next) {
		String s = next.getSubject().toString();
		String p = next.getPredicate().toString();
		String o = next.getObject().toString();
		
		logger.trace(s+" "+p+" "+o);
		
		long si = this.dictionnary.add(s);
		long pi = this.dictionnary.add(p);
		long oi = this.dictionnary.add(o);
		
		this.tripleStore.add(new TripleImplNaive(si, pi, oi));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dictionnary == null) ? 0 : dictionnary.hashCode());
		result = prime * result
				+ ((tripleStore == null) ? 0 : tripleStore.hashCode());
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
		ParserImplNaive other = (ParserImplNaive) obj;
		if (dictionnary == null) {
			if (other.dictionnary != null)
				return false;
		} else if (!dictionnary.equals(other.dictionnary))
			return false;
		if (tripleStore == null) {
			if (other.tripleStore != null)
				return false;
		} else if (!tripleStore.equals(other.tripleStore))
			return false;
		return true;
	}
	
}
