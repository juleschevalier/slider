package fr.ujm.tse.lt2c.satin.tools;

import fr.ujm.tse.lt2c.satin.dictionnary.DictionnaryImplNaive;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

public class Comparator {
	
	TripleStore ground_triples;
	Dictionnary ground_dictionary;
	
	public Comparator(String file){
		
		this.ground_triples = new VerticalPartioningTripleStoreRWLock();
		this.ground_dictionary = new DictionnaryImplNaive();
		Parser parser = new ParserImplNaive(this.ground_dictionary, this.ground_triples);
		parser.parse(file);
		
	}
	
	public long compare(TripleStore triples, Dictionnary dictionary){
//		if(triples==null){
//			System.err.println("Null");
//			return false;
//		}
		
//		if(this.ground_triples.size() != triples.size()){
//			size=
//			return false;
//		}
		
//		for (Triple triple : this.ground_triples.getAll()) {
//			if(!triples.contains(triple)){
//				System.err.println("missing : "+this.ground_dictionary.printTriple(triple));
//				return false;
//			}
//		}
//		
//		for (Triple triple : triples.getAll()) {
//			if(!this.ground_triples.contains(triple)){
//				System.err.println("added : "+this.ground_dictionary.printTriple(triple));
//				return false;
//			}
//		}
		
//		return true;
		
		return (triples.size()-ground_triples.size());
	}

}
