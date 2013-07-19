package fr.ujm.tse.lt2c.satin.naiveImpl.rules;

import java.util.Collection;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;

/**
 * 	INPUT
 * x owl:sameAs y
 * x owl:differentFrom y
 *  OUPUT
 * false
 */
public class NaiveEQ_DIFF1 implements Rule {

	private static Logger logger = Logger.getLogger(NaiveEQ_DIFF1.class);
	private Dictionnary dictionnary;
	private TripleStore tripleStore;

	public NaiveEQ_DIFF1(Dictionnary dictionnary, TripleStore tripleStore) {
		super();
		this.dictionnary = dictionnary;
		this.tripleStore = tripleStore;
	}
	
	@Override
	public void run() {



		/*
		 * Get concepts codes in dictionnary
		 */
		long sameAs = dictionnary.add("http://www.w3.org/2002/07/owl#sameAs");
		long differentFrom = dictionnary.add("http://www.w3.org/2002/07/owl#http://www.w3.org/2002/07/owl#differentFrom");

		/*
		 * Get triples matching input 
		 * Create
		 */
		Collection<Triple> sameAs_Triples = tripleStore.getbyPredicate(sameAs);
		Collection<Triple> differentFrom_Triples = tripleStore.getbyPredicate(differentFrom);

		for (Triple t1 : sameAs_Triples) {
			long s1=t1.getSubject(), o1=t1.getObject();

			for (Triple t2 : differentFrom_Triples) {
				long s2=t2.getSubject(), o2=t2.getObject();

				if(s1==s2&&o1==o2){
					logger.trace("EQ_REP_S "+dictionnary.printTriple(t1)+" + "+dictionnary.printTriple(t2)+" -> FALSE");
				}
			}
		}
	}

}
