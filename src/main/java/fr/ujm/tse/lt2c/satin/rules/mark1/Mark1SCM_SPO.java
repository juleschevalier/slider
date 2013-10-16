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
 * INPUT p3 rdfs:subPropertyOf p2 p2 rdfs:subPropertyOf p3 OUPUT p3
 * rdfs:subPropertyOf p3
 */
public class Mark1SCM_SPO extends AbstractRule {

	private static Logger logger = Logger.getLogger(Mark1SCM_SPO.class);

	public Mark1SCM_SPO(Dictionary dictionary, TripleStore usableTriples,
			Collection<Triple> newTriples, TripleStore tripleStore,
			CountDownLatch doneSignal) {
		super(dictionary, tripleStore, usableTriples, newTriples, "SCM_SPO",
				doneSignal);

	}

	protected int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples) {

		long subPropertyOf = AbstractDictionary.subPropertyOf;

		int loops = 0;

		Multimap<Long, Long> subpropertyMultimap = ts1.getMultiMapForPredicate(subPropertyOf);
		if (subpropertyMultimap != null && subpropertyMultimap.size() > 0) {

			Collection<Triple> subpropertyTriples = ts2.getbyPredicate(subPropertyOf);

			/* For each type triple */
			for (Triple triple : subpropertyTriples) {
				/*
				 * Get all objects (p3) of subPropertyOf triples with
				 */
				Collection<Long> p3s = subpropertyMultimap.get(triple.getObject());
				loops++;
				for (Long p3 : p3s) {

					if (p3 != triple.getSubject()) {

						Triple result = new TripleImplNaive(triple.getSubject(), subPropertyOf, p3);
						outputTriples.add(result);

						logTrace(dictionary
								.printTriple(new TripleImplNaive(triple.getSubject(), subPropertyOf, triple.getObject()))
								+ " & "
								+ dictionary.printTriple(new TripleImplNaive(triple.getObject(), subPropertyOf, triple.getSubject()))
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
