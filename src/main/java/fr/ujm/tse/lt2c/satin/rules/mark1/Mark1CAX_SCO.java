package fr.ujm.tse.lt2c.satin.rules.mark1;

import java.util.Collection;
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
 * INPUT c1 rdfs:subClassOf c2 x rdf:type c1 OUPUT x rdf:type c2
 */
public class Mark1CAX_SCO extends AbstractRule {

	private static Logger logger = Logger.getLogger(Mark1CAX_SCO.class);

	public Mark1CAX_SCO(Dictionnary dictionnary, TripleStore usableTriples,
			Collection<Triple> newTriples, TripleStore tripleStore,
			CountDownLatch doneSignal) {
		super(dictionnary, tripleStore, usableTriples, newTriples, "CAX_SCO",
				doneSignal);

	}

	@Override
	public void run() {

		/*
		 * Get concepts codes in dictionnary
		 */
		long subClassOf = AbstractDictionnary.subClassOf;
		long type = AbstractDictionnary.type;

		long loops = 0;

		Collection<Triple> outputTriples = new HashSet<>();

		if (usableTriples.isEmpty()) {
			Multimap<Long, Long> subclassMultimap = tripleStore
					.getMultiMapForPredicate(subClassOf);
			if (subclassMultimap.size() == 0)
				return;
			Collection<Triple> types = tripleStore.getbyPredicate(type);
			for (Triple triple : types) {
				Collection<Long> c2s = subclassMultimap.get(triple.getObject());
				loops++;
				for (Long c2 : c2s) {

					Triple result = new TripleImplNaive(triple.getSubject(),
							type, c2);
					outputTriples.add(result);
				}
			}
		} else {

		}

		// for (Triple t1 : mytriples) {
		// // System.out.println(mytriples.size());
		// long s1 = t1.getSubject(), p1 = t1.getPredicate(), o1 = t1
		// .getObject();
		//
		// if (!(p1 == subClassOf || p1 == type))
		// continue;
		// Collection<Triple> mytriples2 = p1 == subClassOf ? type_Triples
		// : subClassOf_Triples;
		// System.out.println(mytriples2.size());
		// for (Triple t2 : mytriples2) {
		//
		// long s2 = t2.getSubject(), o2 = t2.getObject();
		// loops++;
		//
		// if (p1 == subClassOf && s1 == o2) {
		// Triple result = new TripleImplNaive(s2, type, o1);
		// logTrace(dictionnary.printTriple(t1) + " & "
		// + dictionnary.printTriple(t2) + " -> "
		// + dictionnary.printTriple(result));
		// outputTriples.add(result);
		// }
		//
		// if (p1 == type && s2 == o1) {
		// Triple result = new TripleImplNaive(s1, type, o2);
		// logTrace(dictionnary.printTriple(t1) + " & "
		// + dictionnary.printTriple(t2) + " -> "
		// + dictionnary.printTriple(result));
		// outputTriples.add(result);
		// }
		// }
		//
		// }

		addNewTriples(outputTriples);

		logDebug(this.getClass() + " : " + loops
				+ " iterations - outputTriples  " + outputTriples.size());
		finish();
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
