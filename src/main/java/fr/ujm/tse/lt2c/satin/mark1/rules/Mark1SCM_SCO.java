package fr.ujm.tse.lt2c.satin.mark1.rules;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleImplNaive;

public class Mark1SCM_SCO implements Rule {

	private static Logger logger = Logger.getLogger(Mark1SCM_SCO.class);
	private Dictionnary dictionnary;
	private TripleStore tripleStore;
	private Collection<Triple> usableTriples;
	Collection<Triple> newTriples;

	public Mark1SCM_SCO(Dictionnary dictionnary, Collection<Triple> usableTriples,  Collection<Triple> newTriples, TripleStore tripleStore) {
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
		 * c1 rdfs:subClassOf c2
		 * c2 rdfs:subClassOf c3
		 *  OUPUT
		 * c1 rdfs:subClassOf c3
		 */

		/*
		 * Get concepts codes in dictionnary
		 */
		long subClassOf = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#subClassOf");

		long loops = 0;

		/*
		 * Get triples matching input 
		 * Create
		 */
		Collection<Triple> subClassOf_Triples = tripleStore.getbyPredicate(subClassOf);
		Collection<Triple> outputTriples = new HashSet<>();
		
		if (usableTriples.isEmpty()) { // We use the entire triplestore

			for (Triple t1 : subClassOf_Triples) {
				long s1=t1.getSubject(), o1=t1.getObject();

				for (Triple t2 : subClassOf_Triples) {
					long s2=t2.getSubject(), o2=t2.getObject();

					loops++;
					if(o1==s2){
						Triple result = new TripleImplNaive(s1, subClassOf, o2);
						logger.trace("F SCM_SCO "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
						outputTriples.add(result);
					}

				}

			}
		}else{ //There are usable triples, so we just manage with them

			for (Triple t1 : usableTriples) {
				long s1 = t1.getSubject(), p1=t1.getPredicate(), o1 = t1.getObject();

				if(p1==subClassOf){

					for (Triple t2 : subClassOf_Triples) {
						long s2 = t2.getSubject(), o2 = t2.getObject();
						loops++;

						if(o1==s2){
							Triple result = new TripleImplNaive(s1, subClassOf, o2);
							logger.trace("SCM_SCO " + dictionnary.printTriple(t1)+ " & " + dictionnary.printTriple(t2) + " -> "+ dictionnary.printTriple(result));
							outputTriples.add(result);
						}
						if(o2==s1){
							Triple result = new TripleImplNaive(s2, subClassOf, o1);
							logger.trace("SCM_SCO " + dictionnary.printTriple(t1)+ " & " + dictionnary.printTriple(t2) + " -> "+ dictionnary.printTriple(result));
							outputTriples.add(result);
						}
					}
				}
			}

		}
		for (Triple triple : outputTriples) {
			if(!tripleStore.getAll().contains(triple)){
				tripleStore.add(triple);
				newTriples.add(triple);

			}else{
				logger.trace((usableTriples.isEmpty()?"F SCM_SCO ":"SCM_SCO") + dictionnary.printTriple(triple)+" already present");
			}
		}
		//		tripleStore.addAll(outputTriples);
		//		newTriples.addAll(outputTriples);
		logger.debug(this.getClass()+" : "+loops+" iterations");

	}

}
