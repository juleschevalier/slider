package fr.ujm.tse.lt2c.satin.rules.mark1mt;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.dictionnary.AbstractDictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.rules.AbstractRuleMT;
import fr.ujm.tse.lt2c.satin.triplestore.TripleImplNaive;

/**
 * INPUT p rdfs:range c x p y OUPUT y rdf:type c
 */
public class Mark1mtPRP_RNG extends AbstractRuleMT {

	private static Logger logger = Logger.getLogger(Mark1mtPRP_RNG.class);

	public Mark1mtPRP_RNG(Dictionnary dictionnary, TripleStore usableTriples,
			Collection<Triple> newTriples, TripleStore tripleStore) {
		super();
		this.dictionnary = dictionnary;
		this.tripleStore = tripleStore;
		this.usableTriples = usableTriples;
		this.newTriples = newTriples;
		this.ruleName = "PRP_RNG";
	}

	@Override
	public void run() {

		/*
		 * Get concepts codes needed from dictionnary
		 */
		long range = AbstractDictionnary.range;
		long type = AbstractDictionnary.type;

		long loops = 0;

		Collection<Triple> outputTriples = new HashSet<>();

		Collection<Triple> range_Triples = tripleStore.getbyPredicate(range);
		Collection<Triple> predicate_Triples;

		/* Use all the triplestore */
		if (usableTriples.isEmpty()) {
			for (Triple t1 : range_Triples) {
				long s1 = t1.getSubject(), o1 = t1.getObject();
				predicate_Triples = tripleStore.getbyPredicate(s1);

				for (Triple t2 : predicate_Triples) {
					long o2 = t2.getObject();

					Triple result = new TripleImplNaive(o2, type, o1);
					logTrace(dictionnary.printTriple(t1) + " & "
							+ dictionnary.printTriple(t2) + " -> "
							+ dictionnary.printTriple(result));
					outputTriples.add(result);

				}

			}
		}
		/* Use usableTriples */
		else {
			for (Triple t1 : usableTriples.getAll()) {
				long s1 = t1.getSubject(), p1 = t1.getPredicate(), o1 = t1
						.getObject();

				if (p1 == range) {
					predicate_Triples = tripleStore.getAll();
				} else
					predicate_Triples = range_Triples;

				for (Triple t2 : predicate_Triples) {
					long s2 = t2.getSubject(), p2 = t2.getPredicate(), o2 = t2
							.getObject();
					loops++;

					if (p1 == type && s1 == p2) {
						Triple result = new TripleImplNaive(o2, type, o1);
						logTrace(dictionnary.printTriple(t1) + " & "
								+ dictionnary.printTriple(t2) + " -> "
								+ dictionnary.printTriple(result));
						outputTriples.add(result);
					}
					if (p2 == type && s2 == p1) {
						Triple result = new TripleImplNaive(o1, type, o2);
						logTrace(dictionnary.printTriple(t2) + " & "
								+ dictionnary.printTriple(t1) + " -> "
								+ dictionnary.printTriple(result));
						outputTriples.add(result);
					}
				}
			}
		}

		addNewTriples(outputTriples);

		logDebug(this.getClass() + " : " + loops + " iterations");
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
