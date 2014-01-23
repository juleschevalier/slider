package fr.ujm.tse.lt2c.satin.rules.run;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.google.common.collect.Multimap;

import fr.ujm.tse.lt2c.satin.dictionary.AbstractDictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;

/**
 * INPUT
 * c1 rdfs:subClassOf c2
 * x rdf:type c1
 * OUPUT
 * x rdf:type c2
 */
public class RunCAX_SCO extends AbstractRun {

    private static final Logger logger = Logger.getLogger(RunCAX_SCO.class);
    private static final long[] INPUT_MATCHERS = { AbstractDictionary.subClassOf, AbstractDictionary.type };
    private static final long[] OUTPUT_MATCHERS = { AbstractDictionary.type };

    public RunCAX_SCO(Dictionary dictionary, TripleStore tripleStore, AtomicInteger phaser) {
        super(dictionary, tripleStore, phaser, "CAX_SCO");

    }

    @Override
    protected int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples) {

        long subClassOf = AbstractDictionary.subClassOf;
        long type = AbstractDictionary.type;

        int loops = 0;

        Multimap<Long, Long> subclassMultimap = ts1.getMultiMapForPredicate(subClassOf);
        if (subclassMultimap != null && !subclassMultimap.isEmpty()) {

            HashMap<Long, Collection<Long>> cachePredicates = new HashMap<>();

            Collection<Triple> types = ts2.getbyPredicate(type);
            for (Triple typeTriple : types) {

                Collection<Long> c2s;
                if (!cachePredicates.containsKey(typeTriple.getObject())) {
                    c2s = subclassMultimap.get(typeTriple.getObject());
                    cachePredicates.put(typeTriple.getObject(), c2s);
                } else {
                    c2s = cachePredicates.get(typeTriple.getObject());
                }

                loops++;
                for (Long c2 : c2s) {

                    if (typeTriple.getSubject() >= 0) {
                        Triple result = new ImmutableTriple(typeTriple.getSubject(), type, c2);
                        outputTriples.add(result);
                        logTrace(dictionary.printTriple(new ImmutableTriple(typeTriple.getSubject(), type, typeTriple.getObject())) + " & " + dictionary.printTriple(new ImmutableTriple(typeTriple.getObject(), subClassOf, c2)) + " -> " + dictionary.printTriple(result));
                    }
                }
            }
        }

        return loops;

    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public long[] getInputMatchers() {
        return INPUT_MATCHERS;
    }

    @Override
    public long[] getOutputMatchers() {
        return OUTPUT_MATCHERS;
    }

}
