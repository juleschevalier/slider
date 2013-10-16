package fr.ujm.tse.lt2c.satin.rules.mark1;

import java.util.Collection;
import java.util.HashMap;
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

	protected int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples) {

		long subPropertyOf = AbstractDictionary.subPropertyOf;

		int loops = 0;

		Multimap<Long, Long> subPropertyOfMultiMap = ts1.getMultiMapForPredicate(subPropertyOf);
		if (subPropertyOfMultiMap != null && subPropertyOfMultiMap.size() > 0) {

			HashMap<Long, Collection<Triple>> cachePredicates = new HashMap<>();

			for (Long p1 : subPropertyOfMultiMap.keySet()) {

				Collection<Triple> matchingTriples;
				if (!cachePredicates.containsKey(p1)) {
					matchingTriples = ts2.getbyPredicate(p1);
					cachePredicates.put(p1, matchingTriples);
				} else {
					matchingTriples = cachePredicates.get(p1);
				}

				for (Triple triple : matchingTriples) {

					for (Long p2 : subPropertyOfMultiMap.get(p1)) {

						Triple result = new TripleImplNaive(triple.getSubject(), p2, triple.getObject());
						logTrace(dictionary.printTriple(triple)
								+ " & "
								+ dictionary.printTriple(new TripleImplNaive(p1, subPropertyOf, p2))
								+ " -> "
								+ dictionary.printTriple(result));
						outputTriples.add(result);
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
