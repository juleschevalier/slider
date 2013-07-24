package fr.ujm.tse.lt2c.satin.naiveImpl;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;

public class RDFtoRAW {
	
	public static void main(String[] args) {
		TripleStore tripleStore = new TripleStoreImplNaive();
		Dictionnary dictionnary = new DictionnaryImplNaive();
		Parser parser = new ParserImplNaive(dictionnary , tripleStore);
		String in = "geopolitical_by_jena.owl", out = "geopolitical_by_jena_.owl";
		
		parser.parse(in);
		tripleStore.writeToFile(out, dictionnary);
	}

}
