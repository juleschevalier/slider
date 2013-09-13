package fr.ujm.tse.lt2c.satin.rules.mark1;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.google.common.collect.Multimap;

import fr.ujm.tse.lt2c.satin.dictionnary.AbstractDictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.rules.AbstractRule;
import fr.ujm.tse.lt2c.satin.triplestore.TripleImplNaive;

/**
 * INPUT
 *  p rdfs:domain c 
 *  x p y
 * OUPUT 
 *  x rdf:type c
 */
public class Mark1PRP_DOM extends AbstractRule {

	private static Logger logger = Logger.getLogger(Mark1PRP_DOM.class);

	public Mark1PRP_DOM(Dictionnary dictionnary, TripleStore usableTriples,
			Collection<Triple> newTriples, TripleStore tripleStore,
			CountDownLatch doneSignal) {
		super(dictionnary, tripleStore, usableTriples, newTriples, "PRP_DOM",
				doneSignal);

	}

	@Override
	public void run() {

		/*
		 * Get concepts codes needed from dictionnary
		 */
		long domain = AbstractDictionnary.domain;
		long type = AbstractDictionnary.type;

		long loops = 0;

		Collection<Triple> outputTriples = new HashSet<>();

		Collection<Triple> domain_Triples = tripleStore.getbyPredicate(domain);
		Collection<Triple> predicate_Triples;

		/* Use all the triplestore */
		if (usableTriples.isEmpty()) {
			for (Triple t1 : domain_Triples) {
				long s1 = t1.getSubject(), o1 = t1.getObject();
				predicate_Triples = tripleStore.getbyPredicate(s1);

				for (Triple t2 : predicate_Triples) {
					long s2 = t2.getSubject();

					Triple result = new TripleImplNaive(s2, type, o1);
					logTrace(dictionnary.printTriple(t1) + " & "
							+ dictionnary.printTriple(t2) + " -> "
							+ dictionnary.printTriple(result));
					outputTriples.add(result);

				}

			}
		}
		/* Use usableTriples */
		else {

			HashMap<Long, Collection<Triple>> cache = new HashMap<>();
			// Case 1, all p of rdfs:domain in usabletriple
			for (Triple t1 : usableTriples.getbyPredicate(domain)) {
				long p = t1.getSubject();
				long c = t1.getObject();

				Collection<Triple> triples = null;
				if (!cache.containsKey(p)) {
					triples = tripleStore.getbyPredicate(p);
					cache.put(p, triples);
				} else {
					triples = cache.get(p);
				}

				for (Triple triple : triples) {
					Triple result = new TripleImplNaive(triple.getSubject(),
							type, c);
					logTrace(dictionnary.printTriple(t1) + " & "
							+ dictionnary.printTriple(triple) + " -> "
							+ dictionnary.printTriple(result));
					outputTriples.add(result);
				}
			}
			// Case 2, all x p y in usable triple
			// Set up a cache for multimaps
			Multimap<Long, Long> map = tripleStore
					.getMultiMapForPredicate(domain);
			for (Triple t1 : usableTriples.getAll()) {
				long x = t1.getSubject();
				long p = t1.getPredicate();
				Collection<Long> listofc = map.get(p);
				for (Long c : listofc) {
					Triple result = new TripleImplNaive(x, type, c);
					logTrace(dictionnary.printTriple(t1) + " & "
							+ dictionnary.printTriple(new TripleImplNaive(p, domain, c)) + " -> "
							+ dictionnary.printTriple(result));
					outputTriples.add(result);
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