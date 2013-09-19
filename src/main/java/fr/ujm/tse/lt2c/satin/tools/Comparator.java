package fr.ujm.tse.lt2c.satin.tools;

import java.io.BufferedWriter;
import java.io.FileWriter;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.TemporaryVerticalPartioningTripleStoreRWLock;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

public class Comparator {

	public static long compare(String ground, Dictionary dictionary, TripleStore triples){

		TripleStore ground_triples = new VerticalPartioningTripleStoreRWLock();
		TripleStore missing_triples = new TemporaryVerticalPartioningTripleStoreRWLock();
		TripleStore too_triples = new TemporaryVerticalPartioningTripleStoreRWLock();

		Parser parser = new ParserImplNaive(dictionary, ground_triples);
		parser.parse(ground);

		for (Triple ground_triple : ground_triples.getAll()) {
			if(!triples.contains(ground_triple)){
				missing_triples.add(ground_triple);
			}
		}
		for (Triple triple : triples.getAll()) {
			if(!ground_triples.contains(triple)){
				too_triples.add(triple);
			}
		}
		
		System.out.println("Missing triples : "+missing_triples.size());
		System.out.println("Too triples : "+too_triples.size());

		try {
			// Create file
			FileWriter fstream = new FileWriter("missing"+ground, false);
			BufferedWriter out = new BufferedWriter(fstream);
			for (Triple triple : missing_triples.getAll()) {
				out.write(dictionary.printTriple(triple) + "\n");
			}
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		try {
			// Create file
			FileWriter fstream = new FileWriter("too"+ground, false);
			BufferedWriter out = new BufferedWriter(fstream);
			for (Triple triple : too_triples.getAll()) {
				out.write(dictionary.printTriple(triple) + "\n");
			}
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}


		return (missing_triples.size()+too_triples.size());
	}

}
