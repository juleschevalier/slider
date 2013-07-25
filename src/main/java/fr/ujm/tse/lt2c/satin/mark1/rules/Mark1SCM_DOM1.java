
package fr.ujm.tse.lt2c.satin.mark1.rules;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleImplNaive;

public class Mark1SCM_DOM1 implements Rule {

	private static Logger logger = Logger.getLogger(Mark1SCM_DOM1.class);
	private Dictionnary dictionnary;
	private TripleStore tripleStore;
	private Collection<Triple> usableTriples;
	Collection<Triple> newTriples;
	private static String RuleName = "SCM_DOM1";

	public Mark1SCM_DOM1(Dictionnary dictionnary, Collection<Triple> usableTriples,  Collection<Triple> newTriples, TripleStore tripleStore) {
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
		 * p rdfs:domain c1
		 * c1 rdfs:subClassOf c2
		 *  OUPUT
		 * p rdfs:domain c2
		 */

		/*
		 * Get concepts codes in dictionnary
		 */
		long domain = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#domain");
		long subClassOf = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#subClassOf");

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

			Collection<Triple> domain_Triples = tripleStore.getbyPredicate(domain);
			Collection<Triple> subClassOf_Triples = tripleStore.getbyPredicate(subClassOf);

			for (Triple t1 : domain_Triples) {
				long s1=t1.getSubject(), o1=t1.getObject();

				for (Triple t2 : subClassOf_Triples) {
					long s2=t2.getSubject(), o2=t2.getObject();

					if(o1==s2){
						Triple result = new TripleImplNaive(s1, domain, o2);
						logger.trace("F SCM_DOM1 "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
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
					
					if(p1==domain && p2==subClassOf&&o1==s2){
						Triple result = new TripleImplNaive(s1, domain, o2);
						logger.trace("SCM_DOM1 "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
						outputTriples.add(result);
					}
					
					if(p2==domain && p1==subClassOf&&o2==s1){
						Triple result = new TripleImplNaive(s2, domain, o1);
						logger.trace("SCM_DOM1 "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
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
				logger.trace((usableTriples.isEmpty()?"F "+RuleName+" ":RuleName) + dictionnary.printTriple(triple)+" allready present");
			}
		}
		logger.debug(this.getClass()+" : "+loops+" iterations");
	}

}