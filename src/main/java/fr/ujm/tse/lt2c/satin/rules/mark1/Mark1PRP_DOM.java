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

/**
 * 	INPUT
 * p rdfs:domain c
 * x p y
 *  OUPUT
 * x rdf:type c
 */
public class Mark1PRP_DOM extends AbstractRule {

	private static Logger logger = Logger.getLogger(Mark1PRP_DOM.class);

	public Mark1PRP_DOM(Dictionnary dictionnary, TripleStore usableTriples,  Collection<Triple> newTriples, TripleStore tripleStore) {
		super();
		this.dictionnary = dictionnary;
		this.tripleStore = tripleStore;
		this.usableTriples = usableTriples;
		this.newTriples = newTriples;
		this.ruleName = "PRP_DOM";
	}

	@Override
	public void run() {

		/*
		 * Get concepts codes needed from dictionnary
		 */
		long domain = AbstractDictionnary.domain;
		long type = AbstractDictionnary.type;

		long loops = 0;

		Collection<Triple> outputTriples = new HashSet<>();

		Collection<Triple> domain_Triples = tripleStore.getbyPredicate(domain);
		Collection<Triple> predicate_Triples;

		/*Use all the triplestore*/
		if(usableTriples.isEmpty()){
			for (Triple t1 : domain_Triples) {
				long s1 = t1.getSubject(), o1 = t1.getObject();
				predicate_Triples = tripleStore.getbyPredicate(s1);
				
				for (Triple t2 : predicate_Triples) {
					long s2 = t2.getSubject();
					
					Triple result = new TripleImplNaive(s2, type, o1);
					logTrace(dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
					outputTriples.add(result);
					
				}

			}
		}
		/*Use usableTriples*/
		else{
			for (Triple t1 : usableTriples.getAll()) {
				long s1 = t1.getSubject(), p1=t1.getPredicate(), o1 = t1.getObject();

				for (Triple t2 : p1==domain?tripleStore.getAll():domain_Triples) {
					long s2 = t2.getSubject(), p2=t2.getPredicate(), o2 = t2.getObject();
					loops++;

					if(p1==domain && s1==p2){
						Triple result = new TripleImplNaive(s2, type, o1);
						logTrace(dictionnary.printTriple(t1)+ " & " + dictionnary.printTriple(t2) + " -> "+ dictionnary.printTriple(result));
						outputTriples.add(result);
					}
					if(p2==domain && s2==p1){
						Triple result = new TripleImplNaive(s1, type, o2);
						logTrace(dictionnary.printTriple(t2)+ " & " + dictionnary.printTriple(t1) + " -> "+ dictionnary.printTriple(result));
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