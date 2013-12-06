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
 * INPUT p rdfs:domain c x p y OUPUT x rdf:type c
 */
public class RunPRP_DOM extends AbstractRun {

	private static Logger logger = Logger.getLogger(RunPRP_DOM.class);
	public static long[] input_matchers = {};
	public static long[] output_matchers = {AbstractDictionary.type};

	public RunPRP_DOM(Dictionary dictionary, TripleStore tripleStore, CountDownLatch doneSignal) {
		super(dictionary, tripleStore, "PRP_DOM", doneSignal);

	}

	protected int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples) {

		long domain = AbstractDictionary.domain;
		long type = AbstractDictionary.type;

		int loops = 0;

		Multimap<Long, Long> domainMultiMap = ts1.getMultiMapForPredicate(domain);
		if (domainMultiMap != null && domainMultiMap.size() > 0) {

			HashMap<Long, Collection<Triple>> cachePredicates = new HashMap<>();

			for (Long p : domainMultiMap.keySet()) {

				Collection<Triple> matchingTriples;
				if (!cachePredicates.containsKey(p)) {
					matchingTriples = ts2.getbyPredicate(p);
					cachePredicates.put(p, matchingTriples);
				} else {
					matchingTriples = cachePredicates.get(p);
				}

				for (Triple triple : matchingTriples) {

					for (Long c : domainMultiMap.get(p)) {

						if (triple.getSubject() >= 0) {
							Triple result = new ImmutableTriple(triple.getSubject(), type, c);
							logTrace(dictionary.printTriple(triple) + " & " + dictionary.printTriple(new ImmutableTriple(p, domain, c)) + " -> " + dictionary.printTriple(result));
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