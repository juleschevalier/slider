package fr.ujm.tse.lt2c.satin.mark1.rules;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleImplNaive;

public class Mark1PRP_RNG implements Rule {
	
	private static Logger logger = Logger.getLogger(Mark1PRP_RNG.class);
	private Dictionnary dictionnary;
	private TripleStore tripleStore;
	private Collection<Triple> usableTriples;
	Collection<Triple> newTriples;

	public Mark1PRP_RNG(Dictionnary dictionnary, Collection<Triple> usableTriples,  Collection<Triple> newTriples, TripleStore tripleStore) {
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
		 * p rdfs:range c
		 * x p y
		 *  OUPUT
		 * y rdf:type c
		 */
		
		/*
		 * Get/add concepts codes needed from dictionnary
		 */
		long range = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#range");
		long type = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#type");

		long loops = 0;
		
		Collection<Triple> outputTriples = new HashSet<>();

		/*
		 * If usableTriples is null,
		 * we infere over the entire triplestore 
		 */
		if (usableTriples == null) {

			/*
			 * IS THIS A LOOSE OF PERFORMANCE ??? 
			 */
			Collection<Triple> domain_Triples = tripleStore.getbyPredicate(range);
			
			for (Triple t1 : domain_Triples) {
				long s1 = t1.getSubject(), o1 = t1.getObject();

				for (Triple t2 : tripleStore.getAll()) {
					long p2 = t2.getPredicate(), o2 = t2.getObject();
					loops++;
					if (s1 == p2) {
						Triple result = new TripleImplNaive(o2, type, o1);

						logger.trace("F PRP_RNG " + dictionnary.printTriple(t1)+ " & " + dictionnary.printTriple(t2) + " -> "+ dictionnary.printTriple(result));
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
					
					if(p1==range && s1==p2){
						Triple result = new TripleImplNaive(o2, type, o1);
						logger.trace("PRP_RNG " + dictionnary.printTriple(t1)+ " & " + dictionnary.printTriple(t2) + " -> "+ dictionnary.printTriple(result));
						outputTriples.add(result);
					}
					if(p2==range && s2==p1){
						Triple result = new TripleImplNaive(o1, type, o2);
						logger.trace("PRP_RNG " + dictionnary.printTriple(t2)+ " & " + dictionnary.printTriple(t1) + " -> "+ dictionnary.printTriple(result));
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
				logger.debug((usableTriples==null?"F PRP_DOM ":"PRP_DOM") + dictionnary.printTriple(triple)+" allready present");
			}
		}
//		tripleStore.addAll(outputTriples);
//		newTriples.addAll(outputTriples);
		logger.debug(this.getClass()+" : "+loops+" iterations");
	}

}
