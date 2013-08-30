package fr.ujm.tse.lt2c.satin.rules.mark1mt;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.dictionnary.AbstractDictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.rules.AbstractRuleMT;
import fr.ujm.tse.lt2c.satin.triplestore.TripleImplNaive;

/**
 * 	INPUT
 * p1 rdfs:subPropertyOf p2
 * x p1 y
 *  OUPUT
 * x p2 y
 */
public class Mark1mtPRP_SPO1 extends AbstractRuleMT {

	private static Logger logger = Logger.getLogger(Mark1mtPRP_SPO1.class);

	public Mark1mtPRP_SPO1(Dictionnary dictionnary, TripleStore usableTriples,  Collection<Triple> newTriples, TripleStore tripleStore) {
		super();
		this.dictionnary = dictionnary;
		this.tripleStore = tripleStore;
		this.usableTriples = usableTriples;
		this.newTriples = newTriples;
		this.ruleName = "PRP_SPO1";
	}

	@Override
	public void run() {



		/*
		 * Get/add concepts codes needed from dictionnary
		 */
		long subPropertyOf = AbstractDictionnary.subPropertyOf;

		long loops = 0;

		Collection<Triple> outputTriples = new HashSet<>();
		
		Collection<Triple> subPropertyOf_Triples = tripleStore.getbyPredicate(subPropertyOf);
		
		/*
		 * If usableTriples is null,
		 * we infer over the entire triplestore 
		 */
		if (usableTriples.isEmpty()) {


			for (Triple t1 : subPropertyOf_Triples) {
				long s1=t1.getSubject(), o1=t1.getObject();

				for (Triple t2 : tripleStore.getbyPredicate(s1)) {
					long s2=t2.getSubject(), o2=t2.getObject();

					Triple result = new TripleImplNaive(s2, o1, o2);
					logTrace(dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
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

			for (Triple t1 : usableTriples.getAll()) {
				long s1 = t1.getSubject(), p1=t1.getPredicate(), o1 = t1.getObject();

				for (Triple t2 : p1==subPropertyOf?tripleStore.getAll():subPropertyOf_Triples) {
					long s2 = t2.getSubject(), p2=t2.getPredicate(), o2 = t2.getObject();
					loops++;

					if(p1==subPropertyOf && s1==p2){
						Triple result = new TripleImplNaive(s2, o1, o2);
						logTrace(dictionnary.printTriple(t1)+ " & " + dictionnary.printTriple(t2) + " -> "+ dictionnary.printTriple(result));
						outputTriples.add(result);
					}
					if(p2==subPropertyOf && s2==p1){
						Triple result = new TripleImplNaive(s1, o2, o1);
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
