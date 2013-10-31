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
 * c1 rdfs:subClassOf c2
 * x rdf:type c1
 * OUPUT
 * x rdf:type c2
 */
public class Mark1CAX_SCO extends AbstractRule {

	private static Logger logger = Logger.getLogger(Mark1CAX_SCO.class);
	public static long[] matchers = {AbstractDictionary.subClassOf,AbstractDictionary.type};

	public Mark1CAX_SCO(Dictionary dictionary, TripleStore tripleStore, CountDownLatch doneSignal, TripleDistributor distributor, TripleBuffer tripleBuffer) {
		super(dictionary, tripleStore, "CAX_SCO", doneSignal, distributor, tripleBuffer);

	}

	protected int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples) {

		long subClassOf = AbstractDictionary.subClassOf;
		long type = AbstractDictionary.type;

		int loops = 0;

		Multimap<Long, Long> subclassMultimap = ts1.getMultiMapForPredicate(subClassOf);
		if (subclassMultimap != null && subclassMultimap.size() > 0) {

			HashMap<Long, Collection<Long>> cachePredicates = new HashMap<>();

			Collection<Triple> types = ts2.getbyPredicate(type);
			for (Triple type_triple : types) {

				Collection<Long> c2s;
				if (!cachePredicates.containsKey(type_triple.getObject())) {
					c2s = subclassMultimap.get(type_triple.getObject());
					cachePredicates.put(type_triple.getObject(), c2s);
				} else {
					c2s = cachePredicates.get(type_triple.getObject());
				}

				loops++;
				for (Long c2 : c2s) {

					if (type_triple.getSubject() >= 0) {
						Triple result = new ImmutableTriple(type_triple.getSubject(), type, c2);
						outputTriples.add(result);
						logTrace(dictionary.printTriple(new ImmutableTriple(type_triple.getSubject(), type, type_triple.getObject())) + " & " + dictionary.printTriple(new ImmutableTriple(type_triple.getObject(), subClassOf, c2)) + " -> " + dictionary.printTriple(result));
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
