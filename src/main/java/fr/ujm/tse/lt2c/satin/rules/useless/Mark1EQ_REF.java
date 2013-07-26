
package fr.ujm.tse.lt2c.satin.rules.useless;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;


import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.TripleImplNaive;

public class Mark1EQ_REF implements Rule {

	private static Logger logger = Logger.getLogger(Mark1EQ_REF.class);
	private Dictionnary dictionnary;
	private TripleStore tripleStore;
	private Collection<Triple> usableTriples;
	Collection<Triple> newTriples;

	public Mark1EQ_REF(Dictionnary dictionnary, Collection<Triple> usableTriples,  Collection<Triple> newTriples, TripleStore tripleStore) {
		super();
		this.dictionnary = dictionnary;
		this.tripleStore = tripleStore;
		this.usableTriples = usableTriples;
		this.newTriples = newTriples;
	}

	@Override
	public void run() {


		/**
		 * 	INPUT
		 * s p o
		 *  OUPUT
		 * s owl:sameAs s
		 * p owl:sameAs p
		 * o owl:sameAs o
		 */

		/*
		 * Get concepts codes in dictionnary
		 */
		long sameAs = dictionnary.add("http://www.w3.org/2002/07/owl#sameAs");

		long loops = 0;

		/*
		 * Get triples matching input 
		 * Create
		 */
		Collection<Triple> outputTriples = new HashSet<>();
		
		/*
		 * If usableTriples is null,
		 * we infere over the entire triplestore 
		 */
		if (usableTriples.isEmpty()) {

			for (Triple t : tripleStore.getAll()) {
				long s=t.getSubject(), p=t.getPredicate(), o=t.getObject();
				Triple result1 = new TripleImplNaive(s, sameAs, s);
				Triple result2 = new TripleImplNaive(p, sameAs, p);
				Triple result3 = new TripleImplNaive(o, sameAs, o);
				logger.trace("F EQ_REF "+dictionnary.printTriple(t)+" -> "+dictionnary.printTriple(result1)+" & "+dictionnary.printTriple(result2)+" & "+dictionnary.printTriple(result3));
				outputTriples.add(result1);
				outputTriples.add(result2);
				outputTriples.add(result3);
				loops++;
			}
		}
		/*
		 * If usableTriples is not null,
		 * we infere over the matching triples
		 * containing at least one from usableTriples
		 */
		else{

			for (Triple t : usableTriples) {
				long s=t.getSubject(), p=t.getPredicate(), o=t.getObject();
				Triple result1 = new TripleImplNaive(s, sameAs, s);
				Triple result2 = new TripleImplNaive(p, sameAs, p);
				Triple result3 = new TripleImplNaive(o, sameAs, o);
				logger.trace("EQ_REF "+dictionnary.printTriple(t)+" -> "+dictionnary.printTriple(result1)+" & "+dictionnary.printTriple(result2)+" & "+dictionnary.printTriple(result3));
				outputTriples.add(result1);
				outputTriples.add(result2);
				outputTriples.add(result3);
				loops++;
			}

		}
		for (Triple triple : outputTriples) {
			if(!tripleStore.getAll().contains(triple)){
				tripleStore.add(triple);
				newTriples.add(triple);
				
			}else{
				logger.trace((usableTriples.isEmpty()?"F EQ_REF ":"EQ_REF") + dictionnary.printTriple(triple)+" allready present");
			}
		}
		logger.debug(this.getClass()+" : "+loops+" iterations");
	}

}
