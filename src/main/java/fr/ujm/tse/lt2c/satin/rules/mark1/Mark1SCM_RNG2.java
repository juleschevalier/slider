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
 * INPUT
 * p2 rdfs:range c
 * p1 rdfs:subPropertyOf p2
 * OUPUT
 * p1 rdfs:range c
 */
public class Mark1SCM_RNG2 extends AbstractRule {

	private static Logger logger = Logger.getLogger(Mark1SCM_RNG2.class);

	public Mark1SCM_RNG2(Dictionary dictionary, TripleStore usableTriples,
			Collection<Triple> newTriples, TripleStore tripleStore,
			CountDownLatch doneSignal) {
		super(dictionary, tripleStore, usableTriples, newTriples,"SCM_RNG2",
				doneSignal);
		
	}

	@Override
	public void run() {

		try{

		/*
		 * Get concepts codes in dictionnary
		 */
		long range = AbstractDictionary.range;
		long subPropertyOf = AbstractDictionary.subPropertyOf;

		Collection<Triple> range_Triples = tripleStore.getbyPredicate(range);
		Collection<Triple> subPropertyOf_Triples = tripleStore
				.getbyPredicate(subPropertyOf);
		Collection<Triple> predicate_Triples;

		long loops = 0;

		/*
		 * Get triples matching input
		 * Create
		 */
		Collection<Triple> outputTriples = new HashSet<>();

		/*
		 * If usableTriples is null,
		 * we infere over the entire triplestore
		 */
		if (usableTriples.isEmpty()) {

			for (Triple t1 : range_Triples) {
				long s1 = t1.getSubject(), o1 = t1.getObject();

				for (Triple t2 : subPropertyOf_Triples) {
					long s2 = t2.getSubject(), o2 = t2.getObject();

					if (s1 == o2) {
						Triple result = new TripleImplNaive(s2, range, o1);
						logTrace(dictionary.printTriple(t1) + " & "
								+ dictionary.printTriple(t2) + " -> "
								+ dictionary.printTriple(result));
						outputTriples.add(result);
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

			for (Triple t1 : usableTriples.getAll()) {
				long s1 = t1.getSubject(), p1 = t1.getPredicate(), o1 = t1
						.getObject();

				if (p1 == range)
					predicate_Triples = subPropertyOf_Triples;
				else if (p1 == subPropertyOf)
					predicate_Triples = range_Triples;
				else
					continue;

				for (Triple t2 : predicate_Triples) {
					long s2 = t2.getSubject(), p2 = t2.getPredicate(), o2 = t2
							.getObject();
					loops++;

					if (p1 == range && p2 == subPropertyOf && s1 == o2) {
						Triple result = new TripleImplNaive(s2, range, o1);
						logTrace(dictionary.printTriple(t1) + " & "
								+ dictionary.printTriple(t2) + " -> "
								+ dictionary.printTriple(result));
						outputTriples.add(result);
					}
					if (p2 == range && p1 == subPropertyOf && s2 == o1) {
						Triple result = new TripleImplNaive(s1, range, o2);
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

		
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			finish();

		}
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
