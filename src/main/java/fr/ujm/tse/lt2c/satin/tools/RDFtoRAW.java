package fr.ujm.tse.lt2c.satin.tools;

import fr.ujm.tse.lt2c.satin.dictionnary.DictionnaryImplNaive;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

public class RDFtoRAW {
	
	public static void main(String[] args) {
		TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
		Dictionnary dictionnary = new DictionnaryImplNaive();
		Parser parser = new ParserImplNaive(dictionnary , tripleStore);
		String in = "estat-legis.ttl", out = "estat-legis.out";
		
		parser.parse(in);
		tripleStore.writeToFile(out, dictionnary);
		
		System.out.println("ok");
	}

}
