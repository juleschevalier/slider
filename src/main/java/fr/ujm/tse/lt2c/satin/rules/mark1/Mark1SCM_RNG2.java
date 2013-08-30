
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
 * p2 rdfs:range c
 * p1 rdfs:subPropertyOf p2
 *  OUPUT
 * p1 rdfs:range c
 */
public class Mark1SCM_RNG2 extends AbstractRule {

	private static Logger logger = Logger.getLogger(Mark1SCM_RNG2.class);

	public Mark1SCM_RNG2(Dictionnary dictionnary, TripleStore usableTriples,  Collection<Triple> newTriples, TripleStore tripleStore) {
		super();
		this.dictionnary = dictionnary;
		this.tripleStore = tripleStore;
		this.usableTriples = usableTriples;
		this.newTriples = newTriples;
		this.ruleName = "SCM_RNG2";
	}

	@Override
	public void run() {



		/*
		 * Get concepts codes in dictionnary
		 */
		long range = AbstractDictionnary.range;
		long subPropertyOf = AbstractDictionnary.subPropertyOf;
		
		Collection<Triple> range_Triples = tripleStore.getbyPredicate(range);
		Collection<Triple> subPropertyOf_Triples = tripleStore.getbyPredicate(subPropertyOf);
		Collection<Triple> predicate_Triples;

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

			for (Triple t1 : range_Triples) {
				long s1=t1.getSubject(), o1=t1.getObject();

				for (Triple t2 : subPropertyOf_Triples) {
					long s2=t2.getSubject(), o2=t2.getObject();

					if(s1==o2){
						Triple result = new TripleImplNaive(s2, range, o1);
						logTrace(dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
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
				
				if(p1==range)
					predicate_Triples = subPropertyOf_Triples;
				else if(p1==subPropertyOf)
					predicate_Triples = range_Triples;
				else
					continue;
				
				for (Triple t2 : predicate_Triples) {
					long s2 = t2.getSubject(), p2=t2.getPredicate(), o2 = t2.getObject();
					loops++;
					
					if(p1==range&&p2==subPropertyOf && s1==o2){
						Triple result = new TripleImplNaive(s2, range, o1);
						logTrace(dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
						outputTriples.add(result);						
					}
					if(p2==range&&p1==subPropertyOf && s2==o1){
						Triple result = new TripleImplNaive(s1, range, o2);
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
