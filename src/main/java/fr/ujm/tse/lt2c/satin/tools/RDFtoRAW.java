package fr.ujm.tse.lt2c.satin.tools;

import fr.ujm.tse.lt2c.satin.dictionnary.DictionnaryImplNaive;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;

public class RDFtoRAW {
	
	public static void main(String[] args) {
		TripleStore tripleStore =null;//TODO update
		Dictionnary dictionnary = new DictionnaryImplNaive();
		Parser parser = new ParserImplNaive(dictionnary , tripleStore);
		String in = "geopolitical_200Ko.owl", out = "geopolitical_200Ko.out";
		
		parser.parse(in);
		tripleStore.writeToFile(out, dictionnary);
		
		System.out.println("ok");
	}

}
