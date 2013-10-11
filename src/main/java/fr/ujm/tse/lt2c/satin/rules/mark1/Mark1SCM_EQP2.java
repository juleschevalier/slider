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

			/*
			 * Get concepts codes in dictionnary
			 */

			long loops = 0;

			/*
			 * Get triples matching input
			 * Create
			 */
			long subPropertyOf = AbstractDictionary.subPropertyOf;
			long equivalentProperty = AbstractDictionary.equivalentProperty;

			Collection<Triple> outputTriples = new HashSet<>();

			/*
			 * If usableTriples is null,
			 * we infere over the entire triplestore
			 */
			if (usableTriples.isEmpty()) {

				Multimap<Long, Long> subpropertyMultimap = tripleStore.getMultiMapForPredicate(subPropertyOf);
				if (subpropertyMultimap != null && subpropertyMultimap.size() > 0) {

					Collection<Triple> subpropertyTriples = tripleStore.getbyPredicate(subPropertyOf);

					/* For each type triple */
					for (Triple triple : subpropertyTriples) {
						/*
						 * Get all objects (c1a) of subPropertyOf triples with
						 */
						Collection<Long> c1as = subpropertyMultimap.get(triple.getObject());
						loops++;
						for (Long c1a : c1as) {

							if (c1a == triple.getSubject() && triple.getObject() != triple.getSubject()) {

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

			}
			/*
			 * If usableTriples is not null,
			 * we infere over the matching triples
			 * containing at least one from usableTriples
			 */
			else {

				/* multimap from triplestore */
				Multimap<Long, Long> subpropertyMultimap = tripleStore.getMultiMapForPredicate(subPropertyOf);
				if (subpropertyMultimap != null && subpropertyMultimap.size() > 0) {

					Collection<Triple> subpropertyTriples = usableTriples.getbyPredicate(subPropertyOf);

					/* For each type triple */
					for (Triple triple : subpropertyTriples) {
						/*
						 * Get all objects (c1a) of subPropertyOf triples with
						 */
						Collection<Long> c1as = subpropertyMultimap.get(triple.getObject());
						loops++;
						for (Long c1a : c1as) {

							if (c1a == triple.getSubject() && triple.getObject() != triple.getSubject()) {

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

				/* multimap from usabletriples */
				subpropertyMultimap = usableTriples.getMultiMapForPredicate(subPropertyOf);
				if (subpropertyMultimap != null && subpropertyMultimap.size() > 0) {

					Collection<Triple> subpropertyTriples = tripleStore.getbyPredicate(subPropertyOf);

					/* For each type triple */
					for (Triple triple : subpropertyTriples) {
						/*
						 * Get all objects (c1a) of subPropertyOf triples with
						 */
						Collection<Long> c1as = subpropertyMultimap.get(triple.getObject());
						loops++;
						for (Long c1a : c1as) {

							if (c1a == triple.getSubject() && triple.getObject() != triple.getSubject()) {

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

			}

			addNewTriples(outputTriples);

			logDebug(this.getClass() + " : " + loops + " iterations  - outputTriples  " + outputTriples.size());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			finish();

		}
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
