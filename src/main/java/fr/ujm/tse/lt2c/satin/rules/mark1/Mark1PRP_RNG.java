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
 * INPUT
 * p rdfs:range c
 * x p y
 * OUPUT
 * y rdf:type c
 */
public class Mark1PRP_RNG extends AbstractRule {

	private static Logger logger = Logger.getLogger(Mark1PRP_RNG.class);

	public Mark1PRP_RNG(Dictionary dictionary, TripleStore usableTriples,
			Collection<Triple> newTriples, TripleStore tripleStore, CountDownLatch doneSignal) {
		super(dictionary,tripleStore,usableTriples,newTriples,"PRP_RNG",doneSignal);

	}

	@Override
	public void run() {

		try{

			/*
			 * Get concepts codes needed from dictionnary
			 */
			long range = AbstractDictionary.range;
			long type = AbstractDictionary.type;

			long loops = 0;

			Collection<Triple> outputTriples = new HashSet<>();

			/* Use all the triplestore */
			if (usableTriples.isEmpty()) {

				Multimap<Long, Long> rangeMultiMap = tripleStore.getMultiMapForPredicate(range);
				if (rangeMultiMap == null || rangeMultiMap.size() == 0) {
					finish();
					return;
				}
				for (Long p : rangeMultiMap.keySet()) {
					Collection<Triple> matchingTriples = tripleStore.getbyPredicate(p);
					for (Triple triple : matchingTriples) {
						for (Long c : rangeMultiMap.get(p)) {
							Triple result = new TripleImplNaive(triple.getObject(), type, c);
							logTrace(dictionary.printTriple(triple)
									+ " & "
									+ dictionary.printTriple(new TripleImplNaive(p,range, c)) 
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

				// Case 1, all rdfs:range in usabletriple
				Multimap<Long, Long> rangeMultiMap = usableTriples.getMultiMapForPredicate(range);
				if (rangeMultiMap != null && rangeMultiMap.size() > 0) {
					for (Long p : rangeMultiMap.keySet()) {
						Collection<Triple> matchingTriples = tripleStore.getbyPredicate(p);
						for (Triple triple : matchingTriples) {
							for (Long c : rangeMultiMap.get(p)) {
								Triple result = new TripleImplNaive(triple.getObject(), type, c);
								logTrace(dictionary.printTriple(triple)
										+ " & "
										+ dictionary.printTriple(new TripleImplNaive(p,range, c)) 
										+ " -> " 
										+ dictionary.printTriple(result));
								outputTriples.add(result);
							}

						}
					}
				}
				// Case 2, all x p y in usable triple
				// Set up a cache for multimaps
				rangeMultiMap = tripleStore.getMultiMapForPredicate(range);
				if (rangeMultiMap != null && rangeMultiMap.size() > 0) {
					for (Long p : rangeMultiMap.keySet()) {
						Collection<Triple> matchingTriples = usableTriples.getbyPredicate(p);
						for (Triple triple : matchingTriples) {
							for (Long c : rangeMultiMap.get(p)) {
								Triple result = new TripleImplNaive(triple.getObject(), type, c);
								logTrace(dictionary.printTriple(triple)
										+ " & "
										+ dictionary.printTriple(new TripleImplNaive(p,range, c)) 
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
