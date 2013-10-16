package fr.ujm.tse.lt2c.satin.rules.mark1;

import java.util.Collection;
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
 * c1 rdfs:subClassOf c2
 * c2 rdfs:subClassOf c3
 * OUPUT
 * c1 rdfs:subClassOf c3
 */
public class Mark1SCM_SCO extends AbstractRule {

	private static Logger logger = Logger.getLogger(Mark1SCM_SCO.class);

	public Mark1SCM_SCO(Dictionary dictionary, TripleStore usableTriples,
			Collection<Triple> newTriples, TripleStore tripleStore,
			CountDownLatch doneSignal) {
		super(dictionary, tripleStore, usableTriples, newTriples, "SCM_SCO",
				doneSignal);

	}

	protected int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples) {

		long subClassOf = AbstractDictionary.subClassOf;

		int loops = 0;

		Multimap<Long, Long> subclassMultimap = ts1.getMultiMapForPredicate(subClassOf);
		if (subclassMultimap != null && subclassMultimap.size() > 0) {

			Collection<Triple> subclassTriples = ts2.getbyPredicate(subClassOf);

			/* For each type triple */
			for (Triple triple : subclassTriples) {
				/*
				 * Get all objects (c1a) of subClassOf triples with
				 */
				Collection<Long> c3s = subclassMultimap.get(triple.getObject());
				loops++;
				for (Long c1a : c3s) {

					if (c1a != triple.getSubject()) {

						Triple result = new TripleImplNaive(triple.getSubject(), subClassOf, c1a);
						outputTriples.add(result);

						logTrace(dictionary
								.printTriple(new TripleImplNaive(triple.getSubject(), subClassOf, triple.getObject()))
								+ " & "
								+ dictionary.printTriple(new TripleImplNaive(triple.getObject(), subClassOf, triple.getSubject()))
								+ " -> "
								+ dictionary.printTriple(result));
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
