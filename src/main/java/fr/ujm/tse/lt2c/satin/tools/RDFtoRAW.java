package fr.ujm.tse.lt2c.satin.tools;

import fr.ujm.tse.lt2c.satin.dictionary.DictionaryImplNaive;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

public class RDFtoRAW {

	public static void main(String[] args) {

		convert("subclassof.owl");
	}

	private static void convert(String in) {
		TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
		Dictionary dictionary = new DictionaryImplNaive();
		Parser parser = new ParserImplNaive(dictionary , tripleStore);
		String out = in+".out";

		parser.parse(in);
		tripleStore.writeToFile(out, dictionary);

		System.out.println("ok");
	}

}
