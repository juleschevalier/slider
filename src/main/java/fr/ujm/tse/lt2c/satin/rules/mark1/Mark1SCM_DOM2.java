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
 * p2 rdfs:domain c
 * p1 rdfs:subPropertyOf p2
 * OUPUT
 * p1 rdfs:domain c
 */
public class Mark1SCM_DOM2 extends AbstractRule {

	private static Logger logger = Logger.getLogger(Mark1SCM_DOM2.class);

	public Mark1SCM_DOM2(Dictionary dictionary, TripleStore usableTriples,
			Collection<Triple> newTriples, TripleStore tripleStore,
			CountDownLatch doneSignal) {
		super(dictionary, tripleStore, usableTriples, newTriples, "SCM_DOM2",
				doneSignal);
		
	}

	@Override
	public void run() {

		try {

			/*
			 * Get concepts codes in dictionnary
			 */
			long subPropertyOf = AbstractDictionary.subPropertyOf;
			long domain = AbstractDictionary.domain;

			long loops = 0;

			Collection<Triple> outputTriples = new HashSet<>();

			if (usableTriples.isEmpty()) {

				Multimap<Long, Long> domainMultimap = tripleStore.getMultiMapForPredicate(domain);
				if (domainMultimap != null && domainMultimap.size() > 0) {

					Collection<Triple> subpropertyTriples = tripleStore.getbyPredicate(subPropertyOf);

					/* For each type triple */
					for (Triple triple : subpropertyTriples) {
						/*
						 * Get all objects (c2) of subClassOf triples with
						 * domain
						 * triples
						 * objects as subject
						 */
						Collection<Long> cs = domainMultimap.get(triple.getObject());
						loops++;
						for (Long c : cs) {

							Triple result = new TripleImplNaive(triple.getSubject(), domain, c);
							outputTriples.add(result);

							logTrace(dictionary
									.printTriple(new TripleImplNaive(triple.getSubject(), subPropertyOf, triple.getObject()))
									+ " & "
									+ dictionary.printTriple(new TripleImplNaive(triple.getObject(),domain, c))
									+ " -> "
									+ dictionary.printTriple(result));
						}
					}
				}
			} else {
				/* domain from usableTriples */
				Multimap<Long, Long> domainMultimap = usableTriples.getMultiMapForPredicate(domain);
				if (domainMultimap != null && domainMultimap.size() > 0) {

					Collection<Triple> subpropertyTriples = tripleStore.getbyPredicate(subPropertyOf);

					/* For each type triple */
					for (Triple triple : subpropertyTriples) {
						/*
						 * Get all objects (c2) of subClassOf triples with
						 * domain
						 * triples
						 * objects as subject
						 */
						Collection<Long> cs = domainMultimap.get(triple.getObject());
						loops++;
						for (Long c : cs) {

							Triple result = new TripleImplNaive(triple.getSubject(), domain, c);
							outputTriples.add(result);

							logTrace(dictionary
									.printTriple(new TripleImplNaive(triple.getSubject(), subPropertyOf, triple.getObject()))
									+ " & "
									+ dictionary.printTriple(new TripleImplNaive(triple.getObject(),domain, c))
									+ " -> "
									+ dictionary.printTriple(result));
						}
					}
				}

				/* domain from tripleStore */
				domainMultimap = tripleStore.getMultiMapForPredicate(domain);
				if (domainMultimap != null && domainMultimap.size() > 0) {

					Collection<Triple> subpropertyTriples = usableTriples.getbyPredicate(subPropertyOf);

					/* For each type triple */
					for (Triple triple : subpropertyTriples) {
						/*
						 * Get all objects (c2) of subClassOf triples with
						 * domain
						 * triples
						 * objects as subject
						 */
						Collection<Long> cs = domainMultimap.get(triple.getObject());
						loops++;
						for (Long c : cs) {

							Triple result = new TripleImplNaive(triple.getSubject(), domain, c);
							outputTriples.add(result);

							logTrace(dictionary
									.printTriple(new TripleImplNaive(triple.getSubject(), subPropertyOf, triple.getObject()))
									+ " & "
									+ dictionary.printTriple(new TripleImplNaive(triple.getObject(),domain, c))
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
