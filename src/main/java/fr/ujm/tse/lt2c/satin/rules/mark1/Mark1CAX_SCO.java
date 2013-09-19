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
 * c1 rdfs:subClassOf c2
 * x rdf:type c1
 * OUPUT
 * x rdf:type c2
 */
public class Mark1CAX_SCO extends AbstractRule {

	private static Logger logger = Logger.getLogger(Mark1CAX_SCO.class);

	public Mark1CAX_SCO(Dictionary dictionary, TripleStore usableTriples,
			Collection<Triple> newTriples, TripleStore tripleStore,
			CountDownLatch doneSignal) {
		super(dictionary, tripleStore, usableTriples, newTriples, "CAX_SCO",
				doneSignal);

	}

	@Override
	public void run() {

		try{

			/*
			 * Get concepts codes in dictionnary
			 */
			long subClassOf = AbstractDictionary.subClassOf;
			long type = AbstractDictionary.type;

			long loops = 0;

			Collection<Triple> outputTriples = new HashSet<>();

			if (usableTriples.isEmpty()) {
				
				/* Get all subClassOf triples */
				Multimap<Long, Long> subclassMultimap = tripleStore.getMultiMapForPredicate(subClassOf);
				if (subclassMultimap == null || subclassMultimap.size() == 0){
					finish();
					return;
				}
				
				/* Get all type triples */
				Collection<Triple> types = tripleStore.getbyPredicate(type);
				
				/* For each type triple */
				for (Triple triple : types) {
					/*
					 * Get all objects (c2) of subClassOf triples with type triples
					 * objects as subject
					 */
					Collection<Long> c2s = subclassMultimap.get(triple.getObject());
					loops++;
					for (Long c2 : c2s) {

						Triple result = new TripleImplNaive(triple.getSubject(),type, c2);
						outputTriples.add(result);

						logTrace(dictionary.printTriple(new TripleImplNaive(triple.getSubject(), type, triple.getObject()))
								+ " & "
								+ dictionary.printTriple(new TripleImplNaive(triple.getObject(), subClassOf, c2))
								+ " -> "
								+ dictionary.printTriple(result));
					}
				}
			} else {
				/* subClassOf from usableTriples */
				Multimap<Long, Long> subclassMultimap = usableTriples.getMultiMapForPredicate(subClassOf);
				if (subclassMultimap != null && subclassMultimap.size() > 0) {

					Collection<Triple> types = tripleStore.getbyPredicate(type);
					for (Triple type_triple : types) {
						Collection<Long> c2s = subclassMultimap.get(type_triple.getObject());
						loops++;
						for (Long c2 : c2s) {

							Triple result = new TripleImplNaive(type_triple.getSubject(), type, c2);
							outputTriples.add(result);
							logTrace(dictionary.printTriple(new TripleImplNaive(type_triple.getSubject(), type, type_triple.getObject()))
									+ " & "
									+ dictionary.printTriple(new TripleImplNaive(type_triple.getObject(), subClassOf, c2))
									+ " -> "
									+ dictionary.printTriple(result));
						}
					}
				}



				/* subClassOf from tripleStore */
				subclassMultimap = tripleStore.getMultiMapForPredicate(subClassOf);
				if (subclassMultimap == null || subclassMultimap.size() == 0){
					logTrace("No subClassOf from tripleStore-0");
					finish();
					return;
				}
				
				Collection<Triple> types = usableTriples.getbyPredicate(type);
				for (Triple type_triple : types) {
					Collection<Long> c2s = subclassMultimap.get(type_triple
							.getObject());
					loops++;
					for (Long c2 : c2s) {

						Triple result = new TripleImplNaive(
								type_triple.getSubject(), type, c2);
						outputTriples.add(result);
						logTrace(dictionary.printTriple(new TripleImplNaive(
								type_triple.getSubject(), type, type_triple
								.getObject()))
								+ " & "
								+ dictionary.printTriple(new TripleImplNaive(
										type_triple.getObject(), subClassOf, c2))
										+ " -> " + dictionary.printTriple(result));
					}
				}

			}

			addNewTriples(outputTriples);

			logDebug(this.getClass() + " : " + loops
					+ " iterations - outputTriples  " + outputTriples.size());
			
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
