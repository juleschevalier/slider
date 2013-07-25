package fr.ujm.tse.lt2c.satin.mark1.rules;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleImplNaive;

public class Mark1PRP_DOM implements Rule {

	private static Logger logger = Logger.getLogger(Mark1PRP_DOM.class);
	private Dictionnary dictionnary;
	private TripleStore tripleStore;
	private Collection<Triple> usableTriples;
	Collection<Triple> newTriples;
	private static String RuleName = "PRP_DOM";

	public Mark1PRP_DOM(Dictionnary dictionnary, Collection<Triple> usableTriples,  Collection<Triple> newTriples, TripleStore tripleStore) {
		super();
		this.dictionnary = dictionnary;
		this.tripleStore = tripleStore;
		this.usableTriples = usableTriples;
		this.newTriples = newTriples;
	}

	@Override
	public void run() {

//		System.out.println("PRP Usable "+usableTriples);
//		System.out.println("PRP New "+newTriples);

		/**
		 * 	INPUT
		 * p rdfs:domain c
		 * x p y
		 *  OUPUT
		 * x rdf:type c
		 */

		/*
		 * Get/add concepts codes needed from dictionnary
		 */
		long domain = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#domain");
		long type = dictionnary.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

		long loops = 0;

		Collection<Triple> outputTriples = new HashSet<>();
		
		/*
		 * If usableTriples is null,
		 * we infere over the entire triplestore 
		 */
		if (usableTriples.isEmpty()) {
			
//			logger.debug("PRP "+usableTriples);

			Collection<Triple> domain_Triples = tripleStore.getbyPredicate(domain);

			for (Triple t1 : domain_Triples) {
				long s1 = t1.getSubject(), o1 = t1.getObject();

				for (Triple t2 : tripleStore.getbyPredicate(s1)) {
					long s2 = t2.getSubject();

					loops++;
					Triple result = new TripleImplNaive(s2, type, o1);
					logger.trace("F "+RuleName+" " + dictionnary.printTriple(t1)+ " & " + dictionnary.printTriple(t2) + " -> "+ dictionnary.printTriple(result));
					outputTriples.add(result);
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

					if(p1==domain && s1==p2){
						Triple result = new TripleImplNaive(s2, type, o1);
						logger.trace(RuleName+" " + dictionnary.printTriple(t1)+ " & " + dictionnary.printTriple(t2) + " -> "+ dictionnary.printTriple(result));
						outputTriples.add(result);
					}
					if(p2==domain && s2==p1){
						Triple result = new TripleImplNaive(s1, type, o2);
						logger.trace(RuleName+" " + dictionnary.printTriple(t2)+ " & " + dictionnary.printTriple(t1) + " -> "+ dictionnary.printTriple(result));
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
				logger.trace((usableTriples.isEmpty()?"F "+RuleName+" ":RuleName) + dictionnary.printTriple(triple)+" already present");
			}
		}
		//		tripleStore.addAll(outputTriples);
		//		newTriples.addAll(outputTriples);
		logger.debug(this.getClass()+" : "+loops+" iterations");
	}

}
