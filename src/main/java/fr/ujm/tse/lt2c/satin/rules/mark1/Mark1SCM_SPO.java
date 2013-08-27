
package fr.ujm.tse.lt2c.satin.rules.mark1;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.dictionnary.AbstractDictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.rules.AbstractRule;
import fr.ujm.tse.lt2c.satin.triplestore.TripleImplNaive;

public class Mark1SCM_SPO extends AbstractRule {

	private static Logger logger = Logger.getLogger(Mark1SCM_SPO.class);

	public Mark1SCM_SPO(Dictionnary dictionnary, TripleStore usableTriples,  Collection<Triple> newTriples, TripleStore tripleStore) {
		super();
		this.dictionnary = dictionnary;
		this.tripleStore = tripleStore;
		this.usableTriples = usableTriples;
		this.newTriples = newTriples;
		this.ruleName = "SCM_SPO";
	}

	@Override
	public void run() {


		/**
		 * 	INPUT
		 * p1 rdfs:subPropertyOf p2
		 * p2 rdfs:subPropertyOf p3
		 *  OUPUT
		 * p1 rdfs:subPropertyOf p3
		 */

		/*
		 * Get concepts codes in dictionnary
		 */
		long subPropertyOf = AbstractDictionnary.subPropertyOf;

		long loops = 0;

		/*
		 * Get triples matching input 
		 * Create
		 */
		Collection<Triple> subPropertyOf_Triples = tripleStore.getbyPredicate(subPropertyOf);

		Collection<Triple> outputTriples = new HashSet<>();

		/*
		 * If usableTriples is null,
		 * we infere over the entire triplestore 
		 */
		if (usableTriples.isEmpty()) {


			for (Triple t1 : subPropertyOf_Triples) {
				long s1=t1.getSubject(), o1=t1.getObject();

				for (Triple t2 : subPropertyOf_Triples) {
					long s2=t2.getSubject(), o2=t2.getObject();

					if(o1==s2){
						Triple result = new TripleImplNaive(s1, subPropertyOf, o2);
						logTrace("F SCM_SPO "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
						outputTriples.add(result);
					}

				}

			}

		}
		/*
		 * If usableTriples is not null,
		 * we infere over the matching triples
		 * containing at least one from usableTriples
		 */
		else{

			for (Triple t1 : usableTriples.getAll()) {
				long s1 = t1.getSubject(), p1=t1.getPredicate(), o1 = t1.getObject();

				if(p1!=subPropertyOf)
					continue;

				for (Triple t2 : subPropertyOf_Triples) {
					long s2 = t2.getSubject(), o2 = t2.getObject();
					loops++;

					if(o1==s2){
						Triple result = new TripleImplNaive(s1, subPropertyOf, o2);
						logTrace(dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
						outputTriples.add(result);
					}
					if(o2==s1){
						Triple result = new TripleImplNaive(s2, subPropertyOf, o1);
						logTrace(dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
						outputTriples.add(result);
					}


				}

			}

		}

		addNewTriples(outputTriples);

		logDebug(this.getClass()+" : "+loops+" iterations");

	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
