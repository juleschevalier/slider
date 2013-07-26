
package fr.ujm.tse.lt2c.satin.rules.mark1;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;


import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.TripleImplNaive;

public class Mark1SCM_EQC2 implements Rule {

	private static Logger logger = Logger.getLogger(Mark1SCM_EQC2.class);
	private Dictionnary dictionnary;
	private TripleStore tripleStore;
	private Collection<Triple> usableTriples;
	Collection<Triple> newTriples;
	private static String RuleName = "SCM_EQC2";

	public Mark1SCM_EQC2(Dictionnary dictionnary, Collection<Triple> usableTriples,  Collection<Triple> newTriples, TripleStore tripleStore) {
		super();
		this.dictionnary = dictionnary;
		this.tripleStore = tripleStore;
		this.usableTriples = usableTriples;
		this.newTriples = newTriples;
	}

	@Override
	public void run() {


		/**
		 * c1 rdfs:subClassOf c2
		 * c2 rdfs:subClassOf c1
		 *  OUPUT
		 * c1 owl:equivalentClass c2
		 */

		/*
		 * Get concepts codes in dictionnary
		 */

		long loops = 0;

		/*
		 * Get triples matching input 
		 * Create
		 */
		long subClassOf = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#subClassOf");
		long equivalentClass = dictionnary.add("http://www.w3.org/2002/07/owl#equivalentClass");
		Collection<Triple> outputTriples = new HashSet<>();
		
		/*
		 * If usableTriples is null,
		 * we infere over the entire triplestore 
		 */
		if (usableTriples.isEmpty()) {

			Collection<Triple> subClassOf_Triples = tripleStore.getbyPredicate(subClassOf);

			for (Triple t1 : subClassOf_Triples) {
				long s1=t1.getSubject(), o1=t1.getObject();

				for (Triple t2 : subClassOf_Triples) {
					long s2=t2.getSubject(), o2=t2.getObject();

					if(s1!=o1&&o1==s2&&s1==o2){
						Triple result = new TripleImplNaive(s1, equivalentClass, o1);

						logger.trace("F "+RuleName+" "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
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

			for (Triple t1 : usableTriples) {
				long s1 = t1.getSubject(), p1=t1.getPredicate(), o1 = t1.getObject();

				for (Triple t2 : tripleStore.getAll()) {
					long s2 = t2.getSubject(), p2=t2.getPredicate(), o2 = t2.getObject();
					loops++;

					if(p1==subClassOf && p2==subClassOf){
						if(s1!=o1&&o1==s2&&s1==o2){
							Triple result = new TripleImplNaive(s1, equivalentClass, o1);

							logger.trace(RuleName+" "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
							outputTriples.add(result);
						}
						if(s2!=o2&&o2==s1&&s2==o1){
							Triple result = new TripleImplNaive(s2, equivalentClass, o2);

							logger.trace(RuleName+" "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
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
				logger.trace((usableTriples.isEmpty()?"F "+RuleName+" ":RuleName) + dictionnary.printTriple(triple)+" allready present");
			}
		}
		logger.debug(this.getClass()+" : "+loops+" iterations");
	}

}
