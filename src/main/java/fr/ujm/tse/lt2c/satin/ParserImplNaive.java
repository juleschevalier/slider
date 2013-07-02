package fr.ujm.tse.lt2c.satin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;

import com.hp.hpl.jena.graph.Triple;

public class ParserImplNaive implements Parser {

	private String fileInput = null;
	Dictionnary dico;

	/**
	 * @param f
	 *            the file to parse
	 */
	public ParserImplNaive(String f, Dictionnary dico) {
		System.out.println("[Parser]Started");
		fileInput = f;
		this.dico = dico;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.ujm.tse.lt2c.satin.Parser#parse()
	 */
	@Override
	public void parse() {
		PipedRDFIterator<Triple> iter = new PipedRDFIterator<Triple>();
		final PipedRDFStream<Triple> inputStream = new PipedTriplesStream(iter);

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Runnable parser = new Runnable() {

			public void run() {
				// Call the parsing process.
				System.out.println("run");
				RDFDataMgr.parse(inputStream, fileInput);
				System.out.println("done");
			}
		};

		executor.submit(parser);
		while (iter.hasNext()) {
			Triple next = iter.next();
			addTriple(next);
		}
	}

	private void addTriple(Triple next) {
		System.out.println("new");
	}

	public static void main(String[] args) {

		System.out.println("Start");
		Dictionnary dico = new DictionnaryImplNaive();
		System.out.println("Dico ok");
		Parser parser = new ParserImplNaive(
							"/home/jules/Téléchargements/Ontologie/vicodi-ontology_rdf/vicodi-ontology.rdf",
							dico);
		System.out.println("Parser ok");
		parser.parse();
		System.out.println("Parsing ok");
	}
}
