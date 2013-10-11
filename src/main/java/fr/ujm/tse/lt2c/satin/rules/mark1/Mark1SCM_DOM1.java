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
 * INPUT
 * p rdfs:domain c1
 * c1 rdfs:subClassOf c2
 * OUPUT
 * p rdfs:domain c2
 */
public class Mark1SCM_DOM1 extends AbstractRule {

	private static Logger logger = Logger.getLogger(Mark1SCM_DOM1.class);

	public Mark1SCM_DOM1(Dictionary dictionary, TripleStore usableTriples,
			Collection<Triple> newTriples, TripleStore tripleStore,
			CountDownLatch doneSignal) {
		super(dictionary, tripleStore, usableTriples, newTriples, "SCM_DOM1",
				doneSignal);

	}

	@Override
	public void run() {

		try {

			/*
			 * Get concepts codes in dictionnary
			 */
			long domain = AbstractDictionary.domain;
			long subClassOf = AbstractDictionary.subClassOf;

			long loops = 0;

			Collection<Triple> outputTriples = new HashSet<>();

			if (usableTriples.isEmpty()) {

				Multimap<Long, Long> subclassMultimap = tripleStore.getMultiMapForPredicate(subClassOf);
				if (subclassMultimap == null || subclassMultimap.size() == 0) {
					finish();
					return;
				}

				Collection<Triple> domainTriples = tripleStore.getbyPredicate(domain);

				/* For each type triple */
				for (Triple triple : domainTriples) {
					/*
					 * Get all objects (c2) of subClassOf triples with domain
					 * triples
					 * objects as subject
					 */
					Collection<Long> c2s = subclassMultimap.get(triple
							.getObject());
					loops++;
					for (Long c2 : c2s) {

						Triple result = new TripleImplNaive(triple.getSubject(), domain, c2);
						outputTriples.add(result);

						logTrace(dictionary
								.printTriple(new TripleImplNaive(triple.getSubject(), domain, triple.getObject()))
								+ " & "
								+ dictionary.printTriple(new TripleImplNaive(triple.getObject(), subClassOf, c2))
								+ " -> "
								+ dictionary.printTriple(result));
					}
				}
			} else {
				/* subClassOf from usableTriples */
				Multimap<Long, Long> subclassMultimap = usableTriples
						.getMultiMapForPredicate(subClassOf);
				if (subclassMultimap != null && subclassMultimap.size() > 0) {

					Collection<Triple> domainTriples = tripleStore
							.getbyPredicate(domain);

					/* For each type triple */
					for (Triple triple : domainTriples) {
						/*
						 * Get all objects (c2) of subClassOf triples with
						 * domain
						 * triples
						 * objects as subject
						 */
						Collection<Long> c2s = subclassMultimap.get(triple
								.getObject());
						loops++;
						for (Long c2 : c2s) {

							Triple result = new TripleImplNaive(
									triple.getSubject(), domain, c2);
							outputTriples.add(result);

							logTrace(dictionary.printTriple(new TripleImplNaive(triple.getSubject(), domain, triple.getObject()))
									+ " & "
									+ dictionary.printTriple(new TripleImplNaive(triple.getObject(),subClassOf, c2))
									+ " -> "
									+ dictionary.printTriple(result));
						}
					}
				}

				/* subClassOf from tripleStore */
				subclassMultimap = tripleStore
						.getMultiMapForPredicate(subClassOf);

				if (subclassMultimap != null && subclassMultimap.size() > 0) {

					Collection<Triple> domainTriples = usableTriples.getbyPredicate(domain);

					/* For each type triple */
					for (Triple triple : domainTriples) {
						/*
						 * Get all objects (c2) of subClassOf triples with
						 * domain
						 * triples
						 * objects as subject
						 */
						Collection<Long> c2s = subclassMultimap.get(triple
								.getObject());
						loops++;
						for (Long c2 : c2s) {

							Triple result = new TripleImplNaive(
									triple.getSubject(), domain, c2);
							outputTriples.add(result);

							logTrace(dictionary.printTriple(new TripleImplNaive(triple.getSubject(), domain, triple.getObject()))
									+ " & "
									+ dictionary.printTriple(new TripleImplNaive(triple.getObject(),subClassOf, c2))
									+ " -> "
									+ dictionary.printTriple(result));
						}
					}
				}
			}

			addNewTriples(outputTriples);

			logDebug(this.getClass() + " : " + loops+ " iterations - outputTriples  " + outputTriples.size());

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