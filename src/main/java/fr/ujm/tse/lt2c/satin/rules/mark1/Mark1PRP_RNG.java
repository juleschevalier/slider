package fr.ujm.tse.lt2c.satin.rules.mark1;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.google.common.collect.Multimap;

import fr.ujm.tse.lt2c.satin.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.dictionary.AbstractDictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.rules.AbstractRule;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;

/**
 * INPUT
 * p rdfs:range c
 * x p y
 * OUPUT
 * y rdf:type c
 */
public class Mark1PRP_RNG extends AbstractRule {

	private static Logger logger = Logger.getLogger(Mark1PRP_RNG.class);
	public static long[] matchers = {};

	public Mark1PRP_RNG(Dictionary dictionary, TripleStore tripleStore, CountDownLatch doneSignal, TripleDistributor distributor, TripleBuffer tripleBuffer) {
		super(dictionary, tripleStore, "PRP_RNG", doneSignal, distributor, tripleBuffer);

	}

	protected int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples) {

		long range = AbstractDictionary.range;
		long type = AbstractDictionary.type;

		int loops = 0;

		Multimap<Long, Long> rangeMultiMap = ts1.getMultiMapForPredicate(range);
		if (rangeMultiMap != null && rangeMultiMap.size() > 0) {

			HashMap<Long, Collection<Triple>> cachePredicates = new HashMap<>();

			for (Long p : rangeMultiMap.keySet()) {

				Collection<Triple> matchingTriples;
				if (!cachePredicates.containsKey(p)) {
					matchingTriples = ts2.getbyPredicate(p);
					cachePredicates.put(p, matchingTriples);
				} else {
					matchingTriples = cachePredicates.get(p);
				}

				for (Triple triple : matchingTriples) {

					for (Long c : rangeMultiMap.get(p)) {

						if (triple.getObject() >= 0) {
							Triple result = new ImmutableTriple(triple.getObject(), type, c);
							logTrace(dictionary.printTriple(triple)
									+ " & "
									+ dictionary.printTriple(new ImmutableTriple(p, range, c))
									+ " -> "
									+ dictionary.printTriple(result));
							outputTriples.add(result);
						}
					}
				}
			}
		}

		return loops;

	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
