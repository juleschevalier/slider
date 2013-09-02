package fr.ujm.tse.lt2c.satin.tools;

import fr.ujm.tse.lt2c.satin.dictionnary.DictionnaryImplNaive;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.TripleStoreImplNaive;

public class RDFtoRAW {
	
	public static void main(String[] args) {
		TripleStore tripleStore = new TripleStoreImplNaive();
		Dictionnary dictionnary = new DictionnaryImplNaive();
		Parser parser = new ParserImplNaive(dictionnary , tripleStore);
		String in = "sample1.owl", out = "sample1.out";
		
		parser.parse(in);
		tripleStore.writeToFile(out, dictionnary);
		
		System.out.println("ok");
	}

}
