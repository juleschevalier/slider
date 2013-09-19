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

				/* Get all matching triples */
				Collection<Triple> ranges = tripleStore.getbyPredicate(range);
				if (ranges == null || ranges.size() == 0){
					finish();
					return;
				}

				/* For each triple p range c */
				for (Triple rangeTriple : ranges) {
					/*
					 * Get all triples x p y of triples with p as predicate
					 */
					Collection<Triple> matchingTriples = tripleStore.getbyPredicate(rangeTriple.getSubject());
					loops++;
					for (Triple matchingTriple : matchingTriples) {

						Triple result = new TripleImplNaive(matchingTriple.getObject(),type, rangeTriple.getObject());
						outputTriples.add(result);

						logTrace(dictionary.printTriple(rangeTriple)
								+ " & "
								+ dictionary.printTriple(matchingTriple)
								+ " -> "
								+ dictionary.printTriple(result));
					}
				}
			}
			/* Use usableTriples */
			else {
				// HashMap<Long, Collection<Triple>> cache = new HashMap<>();
				
				// Case 1, all rdfs:range in usabletriple
				Collection<Triple> ranges = usableTriples.getbyPredicate(range);
				if (ranges == null || ranges.size() == 0){
					finish();
					return;
				}
				for (Triple rangeTriple : ranges) {
					long p = rangeTriple.getSubject();
					long c = rangeTriple.getObject();

					Collection<Triple> matchingTriples = tripleStore.getbyPredicate(p);

					// Collection<Triple> matchingTriples = null;
					// if (!cache.containsKey(p)) {
					// matchingTriples = tripleStore.getbyPredicate(p);
					// cache.put(p, matchingTriples);
					// } else {
					// triples = cache.get(p);
					// }

					for (Triple matchingTriple : matchingTriples) {
						Triple result = new TripleImplNaive(matchingTriple.getObject(),type, c);
						outputTriples.add(result);

						logTrace(dictionary.printTriple(rangeTriple) 
								+ " & "
								+ dictionary.printTriple(matchingTriple) 
								+ " -> "
								+ dictionary.printTriple(result));
					}
				}
				// Case 2, all x p y in usable triple
				// Set up a cache for multimaps
				Multimap<Long, Long> rangeMultiMap = tripleStore.getMultiMapForPredicate(range);
				if(rangeMultiMap==null || rangeMultiMap.size()==0){
					finish();
					return;
				}
				for (Triple t1 : usableTriples.getAll()) {
					long y = t1.getObject();
					long p = t1.getPredicate();
					Collection<Long> listofc = rangeMultiMap.get(p);
					for (Long c : listofc) {
						Triple result = new TripleImplNaive(y, type, c);
						outputTriples.add(result);
						
						logTrace(dictionary.printTriple(t1) 
								+ " & "
								+ dictionary.printTriple(new TripleImplNaive(p, range, c)) 
								+ " -> "
								+ dictionary.printTriple(result));
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
