package fr.ujm.tse.lt2c.satin.rules.mark1;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;


import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.TripleImplNaive;

public class Mark1PRP_SPO1 implements Rule {

	private static Logger logger = Logger.getLogger(Mark1PRP_SPO1.class);
	private Dictionnary dictionnary;
	private TripleStore tripleStore;
	private Collection<Triple> usableTriples;
	Collection<Triple> newTriples;

	public Mark1PRP_SPO1(Dictionnary dictionnary, Collection<Triple> usableTriples,  Collection<Triple> newTriples, TripleStore tripleStore) {
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
		 * p1 rdfs:subPropertyOf p2
		 * x p1 y
		 *  OUPUT
		 * x p2 y
		 */

		/*
		 * Get/add concepts codes needed from dictionnary
		 */
		long subPropertyOf = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");

		long loops = 0;

		Collection<Triple> outputTriples = new HashSet<>();
		
		/*
		 * If usableTriples is null,
		 * we infer over the entire triplestore 
		 */
		if (usableTriples.isEmpty()) {

			Collection<Triple> subProperty_Triples = tripleStore.getbyPredicate(subPropertyOf);

			for (Triple t1 : subProperty_Triples) {
				long s1=t1.getSubject(), o1=t1.getObject();

				for (Triple t2 : tripleStore.getbyPredicate(s1)) {
					long s2=t2.getSubject(), o2=t2.getObject();

					Triple result = new TripleImplNaive(s2, o1, o2);
					logger.trace("F PRP_SPO1 "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
					outputTriples.add(result);
				}

			}

		}
		/*
		 * If usableTriples is not null,
		 * we infer over the matching triples
		 * containing at least one from usableTriples
		 */
		else{

			for (Triple t1 : usableTriples) {
				long s1 = t1.getSubject(), p1=t1.getPredicate(), o1 = t1.getObject();

				for (Triple t2 : tripleStore.getAll()) {
					long s2 = t2.getSubject(), p2=t2.getPredicate(), o2 = t2.getObject();
					loops++;

					if(p1==subPropertyOf && s1==p2){
						Triple result = new TripleImplNaive(s2, o1, o2);
						logger.trace("PRP_SPO1 " + dictionnary.printTriple(t1)+ " & " + dictionnary.printTriple(t2) + " -> "+ dictionnary.printTriple(result));
						outputTriples.add(result);
					}
					if(p2==subPropertyOf && s2==p1){
						Triple result = new TripleImplNaive(s1, o2, o1);
						logger.trace("PRP_SPO1 " + dictionnary.printTriple(t2)+ " & " + dictionnary.printTriple(t1) + " -> "+ dictionnary.printTriple(result));
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
				logger.trace((usableTriples.isEmpty()?"F PRP_SPO1 ":"PRP_SPO1") + dictionnary.printTriple(triple)+" allready present");
			}
		}
		//		tripleStore.addAll(outputTriples);
		//		newTriples.addAll(outputTriples);
		logger.debug(this.getClass()+" : "+loops+" iterations");
	}

}
