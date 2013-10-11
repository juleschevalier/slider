package fr.ujm.tse.lt2c.satin.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

public class Comparator {

	public static HashMap<Integer, List<String>> compare(String ground_file, Dictionary dictionary, TripleStore triples){

		TripleStore ground_triples = new VerticalPartioningTripleStoreRWLock();
		List<String> missing_triples = new ArrayList<>();
		List<String> too_triples = new ArrayList<>();

		Parser parser = new ParserImplNaive(dictionary, ground_triples);
		parser.parse(ground_file);

		for (Triple ground_triple : ground_triples.getAll()) {
			if(!triples.contains(ground_triple)){
				missing_triples.add(dictionary.printTriple(ground_triple));
			}
		}
		for (Triple triple : triples.getAll()) {
			if(!ground_triples.contains(triple)){
				too_triples.add(dictionary.printTriple(triple));
			}
		}
		Collections.sort(missing_triples);
		Collections.sort(too_triples);
		
		List<String> tmp = new ArrayList<>(); for (String string : missing_triples) {tmp.add(string);}
		
		missing_triples.removeAll(too_triples);
		too_triples.removeAll(tmp);

		HashMap<Integer, List<String>> return_lists = new HashMap<>();
		return_lists.put(0,missing_triples);
		return_lists.put(1,too_triples);


		return return_lists;
	}

}
