package fr.ujm.tse.lt2c.satin.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

public class Comparator {

	private Comparator() {
	}

	public static Map<Integer, List<String>> compare(String ground_file, Dictionary dictionary, TripleStore triples) {

		TripleStore ground_triples = new VerticalPartioningTripleStoreRWLock();
		List<String> missing_triples = new ArrayList<>();
		List<String> too_triples = new ArrayList<>();

		Parser parser = new ParserImplNaive(dictionary, ground_triples);
		parser.parse(ground_file);

		for (Triple groundTriple : ground_triples.getAll()) {
			if (!triples.contains(groundTriple)) {
				missing_triples.add(dictionary.printTriple(groundTriple));
			}
		}
		for (Triple triple : triples.getAll()) {
			if (!ground_triples.contains(triple)) {
				too_triples.add(dictionary.printTriple(triple));
			}
		}
		Collections.sort(missing_triples);
		Collections.sort(too_triples);

		removeDuplicates(missing_triples, too_triples);

		Map<Integer, List<String>> return_tists = new HashMap<>();
		return_tists.put(0, missing_triples);
		return_tists.put(1, too_triples);

		return return_tists;
	}

	private static void removeDuplicates(List<String> missing_triples, List<String> too_triples) {
		List<String> tmp = new ArrayList<>();
		for (String string : missing_triples) {
			tmp.add(string);
		}

		missing_triples.removeAll(too_triples);
		too_triples.removeAll(tmp);
	}

}
