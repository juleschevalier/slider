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
 * c1 rdfs:subClassOf c2
 * c2 rdfs:subClassOf c3
 * OUPUT
 * c1 rdfs:subClassOf c3
 */
public class RunSCM_SCO extends AbstractRun {

	private static Logger logger = Logger.getLogger(RunSCM_SCO.class);
	public static long[] input_matchers = {AbstractDictionary.subClassOf};
	public static long[] output_matchers = {AbstractDictionary.subClassOf};

	public RunSCM_SCO(Dictionary dictionary, TripleStore tripleStore, CountDownLatch doneSignal) {
		super(dictionary, tripleStore, "SCM_SCO", doneSignal);

	}

	protected int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples) {

		long subClassOf = AbstractDictionary.subClassOf;

		int loops = 0;

		Multimap<Long, Long> subclassMultimap = ts1.getMultiMapForPredicate(subClassOf);
		if (subclassMultimap != null && subclassMultimap.size() > 0) {

			Collection<Triple> subclassTriples = ts2.getbyPredicate(subClassOf);

			HashMap<Long, Collection<Long>> cachePredicates = new HashMap<>();

			/* For each type triple */
			for (Triple triple : subclassTriples) {
				/*
				 * Get all objects (c1a) of subClassOf triples with
				 */

				Collection<Long> c3s;
				if (!cachePredicates.containsKey(triple.getObject())) {
					c3s = subclassMultimap.get(triple.getObject());
					cachePredicates.put(triple.getObject(), c3s);
				} else {
					c3s = cachePredicates.get(triple.getObject());
				}

				loops++;
				for (Long c1a : c3s) {

					if (c1a != triple.getSubject()) {

						Triple result = new ImmutableTriple(triple.getSubject(), subClassOf, c1a);
						outputTriples.add(result);

						logTrace(dictionary.printTriple(new ImmutableTriple(triple.getSubject(), subClassOf, triple.getObject())) + " & " + dictionary.printTriple(new ImmutableTriple(triple.getObject(), subClassOf, triple.getSubject())) + " -> " + dictionary.printTriple(result));
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
