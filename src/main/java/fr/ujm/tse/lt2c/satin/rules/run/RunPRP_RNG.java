package fr.ujm.tse.lt2c.satin.rules.run;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.google.common.collect.Multimap;

import fr.ujm.tse.lt2c.satin.dictionary.AbstractDictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;

/**
 * INPUT
 * p rdfs:range c
 * x p y
 * OUPUT
 * y rdf:type c
 */
public class RunPRP_RNG extends AbstractRun {

	private static Logger logger = Logger.getLogger(RunPRP_RNG.class);
	public static long[] input_matchers = {};
	public static long[] output_matchers = {AbstractDictionary.type};

	public RunPRP_RNG(Dictionary dictionary, TripleStore tripleStore, CountDownLatch doneSignal) {
		super(dictionary, tripleStore, "PRP_RNG", doneSignal);

	}

	protected int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples) {

		long range = AbstractDictionary.range;
		long type = AbstractDictionary.type;

		int loops = 0;

		Multimap<Long, Long> rangeMultiMap = ts1.getMultiMapForPredicate(range);
		if (rangeMultiMap != null && !rangeMultiMap.isEmpty()) {

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

	@Override
	public long[] getInputMatchers() {
		return input_matchers;
	}

	@Override
	public long[] getOutputMatchers() {
		return output_matchers;
	}

}
