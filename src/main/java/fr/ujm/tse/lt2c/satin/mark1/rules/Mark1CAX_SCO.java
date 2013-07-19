
package fr.ujm.tse.lt2c.satin.mark1.rules;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleImplNaive;

public class Mark1CAX_SCO implements Rule {

	private static Logger logger = Logger.getLogger(Mark1Abstract.class);
	private Dictionnary dictionnary;
	private TripleStore tripleStore;
	private Collection<Triple> usableTriples;
	Collection<Triple> newTriples;
	private static String RuleName = "CAX_SCO";

	public Mark1CAX_SCO(Dictionnary dictionnary, Collection<Triple> usableTriples,  Collection<Triple> newTriples, TripleStore tripleStore) {
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
		 * x rdf:type c1
		 *  OUPUT
		 * x rdf:type c2
		 */

		/*
		 * Get concepts codes in dictionnary
		 */
		long subClassOf = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#subClassOf");
		long type = dictionnary.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

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
		if (usableTriples == null) {

			Collection<Triple> subClassOf_Triples = tripleStore.getbyPredicate(subClassOf);
			Collection<Triple> type_Triples = tripleStore.getbyPredicate(type);

			for (Triple t1 : subClassOf_Triples) {
				long s1=t1.getSubject(), o1=t1.getObject();

				for (Triple t2 : type_Triples) {
					long s2=t2.getSubject(), o2=t2.getObject();

					if(s1==o2){
						Triple result = new TripleImplNaive(s2, type, o1);
						logger.trace("CAX_SCO "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
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
					
					if(p1==subClassOf && p2==type && s1==o2){
						Triple result = new TripleImplNaive(s2, type, o1);
						logger.trace("CAX_SCO "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
						outputTriples.add(result);
					}
					
					if(p2==subClassOf && p1==type && s2==o1){
						Triple result = new TripleImplNaive(s1, type, o2);
						logger.trace("CAX_SCO "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
						outputTriples.add(result);
					}
					
				}

			}


		}
		for (Triple triple : outputTriples) {
			if(!tripleStore.getAll().contains(triple)){
				tripleStore.add(triple);
				newTriples.add(triple);

			}else{
				logger.debug((usableTriples==null?"F "+RuleName+" ":RuleName) + dictionnary.printTriple(triple)+" allready present");
			}
		}
		logger.debug(this.getClass()+" : "+loops+" iterations");
	}

}
