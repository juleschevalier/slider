package fr.ujm.tse.lt2c.satin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;

import com.hp.hpl.jena.graph.Triple;

public class Parser {

	private String fileInput = null;

	/**
	 * @param f the file to parse
	 */
	public Parser(String f) {
		fileInput = f;
	}

	public void parse() {
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
			summary.offer(next.getObject().toString());
			summary.offer(next.getSubject().toString());
			summary.offer(next.getPredicate().toString());
		}
	}
}
