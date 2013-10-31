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
 * p2 rdfs:range c
 * p1 rdfs:subPropertyOf p2
 * OUPUT
 * p1 rdfs:range c
 */
public class Mark1SCM_RNG2 extends AbstractRule {

	private static Logger logger = Logger.getLogger(Mark1SCM_RNG2.class);
	public static long[] matchers = {AbstractDictionary.range,AbstractDictionary.subPropertyOf};

	public Mark1SCM_RNG2(Dictionary dictionary, TripleStore tripleStore, CountDownLatch doneSignal, TripleDistributor distributor, TripleBuffer tripleBuffer) {
		super(dictionary, tripleStore, "SCM_RNG2", doneSignal, distributor, tripleBuffer);

	}

	protected int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples) {

		long subPropertyOf = AbstractDictionary.subPropertyOf;
		long range = AbstractDictionary.range;

		int loops = 0;

		Multimap<Long, Long> rangeMultimap = ts1.getMultiMapForPredicate(range);
		if (rangeMultimap != null && rangeMultimap.size() > 0) {

			Collection<Triple> subpropertyTriples = ts2.getbyPredicate(subPropertyOf);

			HashMap<Long, Collection<Long>> cachePredicates = new HashMap<>();

			/* For each type triple */
			for (Triple triple : subpropertyTriples) {
				/*
				 * Get all objects (c2) of subClassOf triples with range triples
				 * objects as subject
				 */

				Collection<Long> cs;
				if (!cachePredicates.containsKey(triple.getObject())) {
					cs = rangeMultimap.get(triple.getObject());
					cachePredicates.put(triple.getObject(), cs);
				} else {
					cs = cachePredicates.get(triple.getObject());
				}

				loops++;
				for (Long c : cs) {

					Triple result = new ImmutableTriple(triple.getSubject(), range, c);
					outputTriples.add(result);

					logTrace(dictionary.printTriple(new ImmutableTriple(triple.getSubject(), subPropertyOf, triple.getObject())) + " & " + dictionary.printTriple(new ImmutableTriple(triple.getObject(), range, c)) + " -> " + dictionary.printTriple(result));
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
