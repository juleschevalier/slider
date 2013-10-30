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
import fr.ujm.tse.lt2c.satin.triplestore.TripleImplNaive;

/**
 * INPUT c1 rdfs:subPropertyOf c2 c2 rdfs:subPropertyOf c1 OUPUT c1
 * owl:equivalentProperty c2
 */
public class Mark1SCM_EQP2 extends AbstractRule {

	private static Logger logger = Logger.getLogger(Mark1SCM_EQP2.class);
	public static long[] matchers = { AbstractDictionary.subPropertyOf };

	public Mark1SCM_EQP2(Dictionary dictionary, TripleStore tripleStore, CountDownLatch doneSignal, TripleDistributor distributor, TripleBuffer tripleBuffer) {
		super(dictionary, tripleStore, "SCM_EQP2", doneSignal, distributor, tripleBuffer);
	}

	protected int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples) {

		long subPropertyOf = AbstractDictionary.subPropertyOf;
		long equivalentProperty = AbstractDictionary.equivalentProperty;

		int loops = 0;

		Multimap<Long, Long> subpropertyMultimap = ts1.getMultiMapForPredicate(subPropertyOf);
		if (subpropertyMultimap != null && subpropertyMultimap.size() > 0) {

			Collection<Triple> subpropertyTriples = ts2.getbyPredicate(subPropertyOf);

			HashMap<Long, Collection<Long>> cachePredicates = new HashMap<>();

			/* For each type triple */
			for (Triple triple : subpropertyTriples) {
				/*
				 * Get all objects (c1a) of subPropertyOf triples with
				 */

				Collection<Long> c1as;
				if (!cachePredicates.containsKey(triple.getObject())) {
					c1as = subpropertyMultimap.get(triple.getObject());
					cachePredicates.put(triple.getObject(), c1as);
				} else {
					c1as = cachePredicates.get(triple.getObject());
				}

				loops++;
				for (Long c1a : c1as) {

					if (c1a == triple.getSubject()/*
												 * && triple.getObject() !=
												 * triple.getSubject()
												 */) {

						Triple result = new TripleImplNaive(triple.getSubject(), equivalentProperty, triple.getObject());
						outputTriples.add(result);

						logTrace(dictionary.printTriple(new TripleImplNaive(triple.getSubject(), subPropertyOf, triple.getObject())) + " & " + dictionary.printTriple(new TripleImplNaive(triple.getObject(), subPropertyOf, triple.getSubject())) + " -> " + dictionary.printTriple(result));
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
