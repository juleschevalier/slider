package fr.ujm.tse.lt2c.satin.rules.mark1;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.google.common.collect.Multimap;

import fr.ujm.tse.lt2c.satin.dictionary.AbstractDictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.rules.AbstractRule;
import fr.ujm.tse.lt2c.satin.triplestore.TripleImplNaive;

/**
 * INPUT p1 rdfs:subPropertyOf p2 x p1 y OUPUT x p2 y
 */
public class Mark1PRP_SPO1 extends AbstractRule {

	private static Logger logger = Logger.getLogger(Mark1PRP_SPO1.class);

	public Mark1PRP_SPO1(Dictionary dictionary, TripleStore usableTriples,
			Collection<Triple> newTriples, TripleStore tripleStore,
			CountDownLatch doneSignal) {
		super(dictionary, tripleStore, usableTriples, newTriples, "PRP_SPO1",
				doneSignal);

	}

	@Override
	public void run() {

		try{

			/*
			 * Get concepts codes needed from dictionnary
			 */
			long subPropertyOf = AbstractDictionary.subPropertyOf;

			long loops = 0;

			Collection<Triple> outputTriples = new HashSet<>();

			/* Use all the triplestore */
			if (usableTriples.isEmpty()) {

				Multimap<Long, Long> subPropertyOfMultiMap = tripleStore.getMultiMapForPredicate(subPropertyOf);
				if (subPropertyOfMultiMap == null || subPropertyOfMultiMap.size() == 0) {
					finish();
					return;
				}
				for (Long p1 : subPropertyOfMultiMap.keySet()) {
					Collection<Triple> matchingTriples = tripleStore.getbyPredicate(p1);
					for (Triple triple : matchingTriples) {
						for (Long p2 : subPropertyOfMultiMap.get(p1)) {
							Triple result = new TripleImplNaive(triple.getSubject(), p2, triple.getObject());
							logTrace(dictionary.printTriple(triple)
									+ " & "
									+ dictionary.printTriple(new TripleImplNaive(p1,subPropertyOf, p2)) 
									+ " -> " 
									+ dictionary.printTriple(result));
							outputTriples.add(result);
						}

					}
				}
			}
			/* Use usableTriples */
			else {
				// HashMap<Long, Collection<Triple>> cache = new HashMap<>();
				// Case 1, all p of rdfs:subPropertyOf in usabletriple
				Multimap<Long, Long> subPropertyOfMultiMap = usableTriples.getMultiMapForPredicate(subPropertyOf);
				if (subPropertyOfMultiMap != null && subPropertyOfMultiMap.size() > 0) {
					for (Long p1 : subPropertyOfMultiMap.keySet()) {
						Collection<Triple> matchingTriples = tripleStore.getbyPredicate(p1);
						for (Triple triple : matchingTriples) {
							for (Long p2 : subPropertyOfMultiMap.get(p1)) {
								Triple result = new TripleImplNaive(triple.getSubject(), p2, triple.getObject());
								logTrace(dictionary.printTriple(triple)
										+ " & "
										+ dictionary.printTriple(new TripleImplNaive(p1,subPropertyOf, p2)) 
										+ " -> " 
										+ dictionary.printTriple(result));
								outputTriples.add(result);
							}

						}
					}
				}
				// Case 2, all x p y in usable triple
				// Set up a cache for multimaps
				subPropertyOfMultiMap = tripleStore.getMultiMapForPredicate(subPropertyOf);
				if (subPropertyOfMultiMap != null && subPropertyOfMultiMap.size() > 0) {
					for (Long p1 : subPropertyOfMultiMap.keySet()) {
						Collection<Triple> matchingTriples = usableTriples.getbyPredicate(p1);
						for (Triple triple : matchingTriples) {
							for (Long p2 : subPropertyOfMultiMap.get(p1)) {
								Triple result = new TripleImplNaive(triple.getSubject(), p2, triple.getObject());
								logTrace(dictionary.printTriple(triple)
										+ " & "
										+ dictionary.printTriple(new TripleImplNaive(p1,subPropertyOf, p2)) 
										+ " -> " 
										+ dictionary.printTriple(result));
								outputTriples.add(result);
							}

						}
					}
				}

			}
			addNewTriples(outputTriples);

			logDebug(this.getClass() + " : " + loops + " iterations  - "+outputTriples.size()+" outputTriples");


		}catch(Exception e){
			e.printStackTrace();
		}finally{
			finish();
		}
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
