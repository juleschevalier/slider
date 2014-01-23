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
 * c1 rdfs:subClassOf c2
 * c2 rdfs:subClassOf c1
 * OUPUT
 * c1 owl:equivalentClass c2
 */
public class RunSCM_EQC2 extends AbstractRun {

    private static final Logger logger = Logger.getLogger(RunSCM_EQC2.class);
    public static final long[] INPUT_MATCHERS = { AbstractDictionary.subClassOf };
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.equivalentClass };

    public RunSCM_EQC2(Dictionary dictionary, TripleStore tripleStore, AtomicInteger phaser) {
        super(dictionary, tripleStore, phaser, "SCM_EQC2");

    }

    @Override
    protected int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples) {

        long subClassOf = AbstractDictionary.subClassOf;
        long equivalentClass = AbstractDictionary.equivalentClass;

        int loops = 0;

        Multimap<Long, Long> subclassMultimap = ts1.getMultiMapForPredicate(subClassOf);
        if (subclassMultimap != null && !subclassMultimap.isEmpty()) {

            Collection<Triple> subclassTriples = ts2.getbyPredicate(subClassOf);

            HashMap<Long, Collection<Long>> cachePredicates = new HashMap<>();

            /* For each type triple */
            for (Triple triple : subclassTriples) {
                /*
                 * Get all objects (c1a) of subClassOf triples with
                 */

                Collection<Long> c1as;
                if (!cachePredicates.containsKey(triple.getObject())) {
                    c1as = subclassMultimap.get(triple.getObject());
                    cachePredicates.put(triple.getObject(), c1as);
                } else {
                    c1as = cachePredicates.get(triple.getObject());
                }

                loops++;
                for (Long c1a : c1as) {

                    if (c1a == triple.getSubject() && triple.getObject() != triple.getSubject()) {

                        Triple result = new ImmutableTriple(triple.getSubject(), equivalentClass, triple.getObject());
                        outputTriples.add(result);

                        logTrace(dictionary.printTriple(new ImmutableTriple(triple.getSubject(), subClassOf, triple.getObject())) + " & " + dictionary.printTriple(new ImmutableTriple(triple.getObject(), subClassOf, triple.getSubject())) + " -> " + dictionary.printTriple(result));
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
