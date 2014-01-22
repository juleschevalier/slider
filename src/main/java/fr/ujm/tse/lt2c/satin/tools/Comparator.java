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

    public static Map<Integer, List<String>> compare(String groundFile, Dictionary dictionary, TripleStore triples) {

        TripleStore groundTriples = new VerticalPartioningTripleStoreRWLock();
        List<String> missingTriples = new ArrayList<>();
        List<String> tooTriples = new ArrayList<>();

        Parser parser = new ParserImplNaive(dictionary, groundTriples);
        parser.parse(groundFile);

        for (Triple groundTriple : groundTriples.getAll()) {
            if (!triples.contains(groundTriple)) {
                missingTriples.add(dictionary.printTriple(groundTriple));
            }
        }
        for (Triple triple : triples.getAll()) {
            if (!groundTriples.contains(triple)) {
                tooTriples.add(dictionary.printTriple(triple));
            }
        }
        Collections.sort(missingTriples);
        Collections.sort(tooTriples);

        removeDuplicates(missingTriples, tooTriples);

        Map<Integer, List<String>> returnLists = new HashMap<>();
        returnLists.put(0, missingTriples);
        returnLists.put(1, tooTriples);

        return returnLists;
    }

    private static void removeDuplicates(List<String> missingTriples, List<String> tooTriples) {
        List<String> tmp = new ArrayList<>();
        for (String string : missingTriples) {
            tmp.add(string);
        }

        missingTriples.removeAll(tooTriples);
        tooTriples.removeAll(tmp);
    }

}
