package fr.ujm.tse.lt2c.satin.rules.mark1;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.dictionary.AbstractDictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.rules.AbstractRule;
import fr.ujm.tse.lt2c.satin.triplestore.TripleImplNaive;

/**
 * INPUT p1 rdfs:subPropertyOf p2 p2 rdfs:subPropertyOf p3 OUPUT p1
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

	@Override
	public void run() {

		/*
		 * Get concepts codes in dictionnary
		 */
		long subPropertyOf = AbstractDictionary.subPropertyOf;

		long loops = 0;

		/*
		 * Get triples matching input Create
		 */
		Collection<Triple> subPropertyOf_Triples = tripleStore
				.getbyPredicate(subPropertyOf);

		Collection<Triple> outputTriples = new HashSet<>();

		/*
		 * If usableTriples is null, we infere over the entire triplestore
		 */
		if (usableTriples.isEmpty()) {

			for (Triple t1 : subPropertyOf_Triples) {
				long s1 = t1.getSubject(), o1 = t1.getObject();

				for (Triple t2 : subPropertyOf_Triples) {
					long s2 = t2.getSubject(), o2 = t2.getObject();

					if (o1 == s2) {
						Triple result = new TripleImplNaive(s1, subPropertyOf,
								o2);
						logTrace("F SCM_SPO " + dictionary.printTriple(t1)
								+ " & " + dictionary.printTriple(t2) + " -> "
								+ dictionary.printTriple(result));
						outputTriples.add(result);
					}

				}

			}

		}
		/*
		 * If usableTriples is not null, we infere over the matching triples
		 * containing at least one from usableTriples
		 */
		else {

			for (Triple t1 : usableTriples.getAll()) {
				long s1 = t1.getSubject(), p1 = t1.getPredicate(), o1 = t1
						.getObject();

				if (p1 != subPropertyOf)
					continue;

				for (Triple t2 : subPropertyOf_Triples) {
					long s2 = t2.getSubject(), o2 = t2.getObject();
					loops++;

					if (o1 == s2) {
						Triple result = new TripleImplNaive(s1, subPropertyOf,
								o2);
						logTrace(dictionary.printTriple(t1) + " & "
								+ dictionary.printTriple(t2) + " -> "
								+ dictionary.printTriple(result));
						outputTriples.add(result);
					}
					if (o2 == s1) {
						Triple result = new TripleImplNaive(s2, subPropertyOf,
								o1);
						logTrace(dictionary.printTriple(t1) + " & "
								+ dictionary.printTriple(t2) + " -> "
								+ dictionary.printTriple(result));
						outputTriples.add(result);
					}

				}

			}

		}

		addNewTriples(outputTriples);

		logDebug(this.getClass() + " : " + loops + " iterations  - outputTriples  " + outputTriples.size());
		finish();

	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
