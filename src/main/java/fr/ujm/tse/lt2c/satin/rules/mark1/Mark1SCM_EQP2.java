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
 * INPUT c1 rdfs:subClassOf c2 c2 rdfs:subClassOf c1 OUPUT c1
 * owl:equivalentClass c2
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

		try{

		/*
		 * Get concepts codes in dictionnary
		 */
		long subClassOf = AbstractDictionary.subClassOf;
		long equivalentClass = AbstractDictionary.equivalentClass;

		long loops = 0;

		/*
		 * Get triples matching input Create
		 */
		Collection<Triple> outputTriples = new HashSet<>();

		Collection<Triple> subClassOf_Triples = tripleStore
				.getbyPredicate(subClassOf);

		/*
		 * If usableTriples is null, we infere over the entire triplestore
		 */
		if (usableTriples.isEmpty()) {

			for (Triple t1 : subClassOf_Triples) {
				long s1 = t1.getSubject(), o1 = t1.getObject();

				for (Triple t2 : subClassOf_Triples) {
					long s2 = t2.getSubject(), o2 = t2.getObject();

					if (s1 != o1 && o1 == s2 && s1 == o2) {
						Triple result = new TripleImplNaive(s1,
								equivalentClass, o1);

						logTrace(dictionary.printTriple(t1) + " & "
								+ dictionary.printTriple(t2) + " -> "
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

				if (p1 != subClassOf)
					continue;

				for (Triple t2 : subClassOf_Triples) {
					long s2 = t2.getSubject(), o2 = t2.getObject();
					loops++;

					if (s1 != o1 && o1 == s2 && s1 == o2) {
						Triple result = new TripleImplNaive(s1,
								equivalentClass, o1);

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
