
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
 * c1 rdfs:subClassOf c2
 * x rdf:type c1
 *  OUPUT
 * x rdf:type c2
 */
public class Mark1CAX_SCO extends AbstractRule{

	private static Logger logger = Logger.getLogger(Mark1CAX_SCO.class);

	public Mark1CAX_SCO(Dictionnary dictionnary, TripleStore usableTriples,  Collection<Triple> newTriples, TripleStore tripleStore) {
		super();
		this.dictionnary = dictionnary;
		this.tripleStore = tripleStore;
		this.usableTriples = usableTriples;
		this.newTriples = newTriples;
		this.ruleName = "CAX_SCO";
	}

	@Override
	public void run() {



		/*
		 * Get concepts codes in dictionnary
		 */
		long subClassOf = AbstractDictionnary.subClassOf;
		long type = AbstractDictionnary.type;

		long loops = 0;

		Collection<Triple> outputTriples = new HashSet<>();



		Collection<Triple> subClassOf_Triples = tripleStore.getbyPredicate(subClassOf);
		Collection<Triple> type_Triples = tripleStore.getbyPredicate(type);

		for (Triple t1 : usableTriples.isEmpty()?subClassOf_Triples:usableTriples.getAll()) {
			long s1 = t1.getSubject(), p1=t1.getPredicate(), o1 = t1.getObject();

			if(!(p1==subClassOf || p1==type))
				continue;

			long predicate_to_find = p1==subClassOf?type:subClassOf;


			for (Triple t2 : p1==subClassOf?type_Triples:subClassOf_Triples) {
				long s2 = t2.getSubject(), o2 = t2.getObject();
				loops++;

				if(predicate_to_find==type && s1==o2){
					Triple result = new TripleImplNaive(s2, type, o1);
					logTrace(dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
					outputTriples.add(result);
				}

				if(predicate_to_find==subClassOf && s2==o1){
					Triple result = new TripleImplNaive(s1, type, o2);
					logTrace(dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
					outputTriples.add(result);
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
