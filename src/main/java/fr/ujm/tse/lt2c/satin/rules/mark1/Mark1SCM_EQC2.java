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
 * c1 rdfs:subClassOf c2
 * c2 rdfs:subClassOf c1
 * OUPUT
 * c1 owl:equivalentClass c2
 */
public class Mark1SCM_EQC2 extends AbstractRule {

	private static Logger logger = Logger.getLogger(Mark1SCM_EQC2.class);

	public Mark1SCM_EQC2(Dictionary dictionary, TripleStore usableTriples,
			Collection<Triple> newTriples, TripleStore tripleStore,
			CountDownLatch doneSignal) {
		super(dictionary, tripleStore, usableTriples, newTriples, "SCM_EQC2",
				doneSignal);

	}

	protected int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples) {

		long subClassOf = AbstractDictionary.subClassOf;
		long equivalentClass = AbstractDictionary.equivalentClass;

		int loops = 0;

		Multimap<Long, Long> subclassMultimap = ts1.getMultiMapForPredicate(subClassOf);
		if (subclassMultimap != null && subclassMultimap.size() > 0) {

			Collection<Triple> subclassTriples = ts2.getbyPredicate(subClassOf);

			/* For each type triple */
			for (Triple triple : subclassTriples) {
				/*
				 * Get all objects (c1a) of subClassOf triples with
				 */
				Collection<Long> c1as = subclassMultimap.get(triple.getObject());
				loops++;
				for (Long c1a : c1as) {

					if (c1a == triple.getSubject() && triple.getObject() != triple.getSubject()) {

						Triple result = new TripleImplNaive(triple.getSubject(), equivalentClass, triple.getObject());
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
