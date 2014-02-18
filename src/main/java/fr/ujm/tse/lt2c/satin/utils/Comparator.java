package fr.ujm.tse.lt2c.satin.utils;

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

    public static Map<Integer, List<String>> compare(final String groundFile, final Dictionary dictionary, final TripleStore triples) {

        final TripleStore groundTriples = new VerticalPartioningTripleStoreRWLock();
        final List<String> missingTriples = new ArrayList<>();
        final List<String> tooTriples = new ArrayList<>();

        final Parser parser = new ParserImplNaive(dictionary, groundTriples);
        parser.parse(groundFile);

        for (final Triple groundTriple : groundTriples.getAll()) {
            if (!triples.contains(groundTriple)) {
                missingTriples.add(dictionary.printTriple(groundTriple));
            }
        }
        for (final Triple triple : triples.getAll()) {
            if (!groundTriples.contains(triple)) {
                tooTriples.add(dictionary.printTriple(triple));
            }
        }
        Collections.sort(missingTriples);
        Collections.sort(tooTriples);

        removeDuplicates(missingTriples, tooTriples);

        final Map<Integer, List<String>> returnLists = new HashMap<>();
        returnLists.put(0, missingTriples);
        returnLists.put(1, tooTriples);

        return returnLists;
    }

    private static void removeDuplicates(final List<String> missingTriples, final List<String> tooTriples) {
        final List<String> tmp = new ArrayList<>();
        for (final String string : missingTriples) {
            tmp.add(string);
        }

        missingTriples.removeAll(tooTriples);
        tooTriples.removeAll(tmp);
    }
    /*
     * Use example
     * if (COMPARE) {
     * final Map<Integer, List<String>> diffTriples = Comparator.compare("jena_"
     * + input, this.dictionary, this.tripleStore);
     * 
     * final List<String> missingTriples = diffTriples.get(0);
     * final List<String> tooTriples = diffTriples.get(1);
     * 
     * // for (Triple t : tripleStore.getAll()) {
     * // System.out.println(dictionary.printTriple(t));
     * // }
     * if ((missingTriples.size() + tooTriples.size()) == 0) {
     * logger.info("Results match");
     * success = true;
     * } else {
     * logger.info("-" + missingTriples.size() + " +" + tooTriples.size());
     * 
     * // for (final String string : missingTriples) {
     * // logger.info("- " + string);
     * // }
     * // for (final String string : tooTriples) {
     * // logger.info("+ " + string);
     * // }
     * }
     * } else {
     * return true;
     * }
     */

}
