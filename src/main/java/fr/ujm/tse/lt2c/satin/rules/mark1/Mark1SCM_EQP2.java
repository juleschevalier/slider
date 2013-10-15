package fr.ujm.tse.lt2c.satin.rules.mark1;

import java.util.Collection;
import java.util.HashSet;
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
 * INPUT c1 rdfs:subPropertyOf c2 c2 rdfs:subPropertyOf c1 OUPUT c1
 * owl:equivalentProperty c2
 */
public class Mark1SCM_EQP2 extends AbstractRule {

	private static Logger logger = Logger.getLogger(Mark1SCM_EQP2.class);

	public Mark1SCM_EQP2(Dictionary dictionary, TripleStore usableTriples,
			Collection<Triple> newTriples, TripleStore tripleStore,
			CountDownLatch doneSignal) {
		super(dictionary, tripleStore, usableTriples, newTriples, "SCM_EQP2",
				doneSignal);
	}
	@Override
	public void run() {

		try {

			long loops = 0;

			Collection<Triple> outputTriples = new HashSet<>();

			if (usableTriples.isEmpty()) {
				loops += process(tripleStore, tripleStore, outputTriples);
			} else {
				loops += process(usableTriples, tripleStore, outputTriples);
				loops += process(tripleStore, usableTriples, outputTriples);
			}

			addNewTriples(outputTriples);

			logDebug(this.getClass() + " : " + loops + " iterations - outputTriples  " + outputTriples.size());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			finish();

		}
	}

	private int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples) {

		long subPropertyOf = AbstractDictionary.subPropertyOf;
		long equivalentProperty = AbstractDictionary.equivalentProperty;

		int loops = 0;

		Multimap<Long, Long> subpropertyMultimap = ts1.getMultiMapForPredicate(subPropertyOf);
		if (subpropertyMultimap != null && subpropertyMultimap.size() > 0) {

			Collection<Triple> subpropertyTriples = ts2.getbyPredicate(subPropertyOf);

			/* For each type triple */
			for (Triple triple : subpropertyTriples) {
				/*
				 * Get all objects (c1a) of subPropertyOf triples with
				 */
				Collection<Long> c1as = subpropertyMultimap.get(triple.getObject());
				loops++;
				for (Long c1a : c1as) {

					if (c1a == triple.getSubject()/* && triple.getObject() != triple.getSubject()*/) {

						Triple result = new TripleImplNaive(triple.getSubject(), equivalentProperty, triple.getObject());
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
