package fr.ujm.tse.lt2c.satin.rules.mark1;

import java.util.Collection;
import java.util.HashMap;
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

	protected int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples) {

		long domain = AbstractDictionary.domain;
		long subClassOf = AbstractDictionary.subClassOf;

		int loops = 0;

		Multimap<Long, Long> subclassMultimap = ts1.getMultiMapForPredicate(subClassOf);
		if (subclassMultimap != null && subclassMultimap.size() > 0) {

			HashMap<Long, Collection<Long>> cachePredicates = new HashMap<>();

			Collection<Triple> domainTriples = ts2.getbyPredicate(domain);

			/* For each type triple */
			for (Triple triple : domainTriples) {
				/*
				 * Get all objects (c2) of subClassOf triples with domain triples objects as subject
				 */
				
				Collection<Long> c2s;
				if(!cachePredicates.containsKey(triple.getObject())){
					c2s = subclassMultimap.get(triple.getObject());
					cachePredicates.put(triple.getObject(), c2s);
				}else{
					c2s = cachePredicates.get(triple.getObject());
				}
				
				loops++;
				for (Long c2 : c2s) {

					Triple result = new TripleImplNaive(triple.getSubject(), domain, c2);
					outputTriples.add(result);

					logTrace(dictionary.printTriple(new TripleImplNaive(triple.getSubject(), domain, triple.getObject()))
							+ " & "
							+ dictionary.printTriple(new TripleImplNaive(triple.getObject(), subClassOf, c2))
							+ " -> "
							+ dictionary.printTriple(result));
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