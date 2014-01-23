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
 * INPUT c1 rdfs:subPropertyOf c2 c2 rdfs:subPropertyOf c1 OUPUT c1
 * owl:equivalentProperty c2
 */
public class RunSCM_EQP2 extends AbstractRun {

    private static final Logger logger = Logger.getLogger(RunSCM_EQP2.class);
    public static final long[] INPUT_MATCHERS = { AbstractDictionary.subPropertyOf };
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.equivalentProperty };

    public RunSCM_EQP2(Dictionary dictionary, TripleStore tripleStore, AtomicInteger phaser) {
        super(dictionary, tripleStore, phaser, "SCM_EQP2");
    }

    @Override
    protected int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples) {

        long subPropertyOf = AbstractDictionary.subPropertyOf;
        long equivalentProperty = AbstractDictionary.equivalentProperty;

        int loops = 0;

        Multimap<Long, Long> subpropertyMultimap = ts1.getMultiMapForPredicate(subPropertyOf);
        if (subpropertyMultimap != null && !subpropertyMultimap.isEmpty()) {

            Collection<Triple> subpropertyTriples = ts2.getbyPredicate(subPropertyOf);

            HashMap<Long, Collection<Long>> cachePredicates = new HashMap<>();

            /* For each type triple */
            for (Triple triple : subpropertyTriples) {
                /*
                 * Get all objects (c1a) of subPropertyOf triples with
                 */

                Collection<Long> c1as;
                if (!cachePredicates.containsKey(triple.getObject())) {
                    c1as = subpropertyMultimap.get(triple.getObject());
                    cachePredicates.put(triple.getObject(), c1as);
                } else {
                    c1as = cachePredicates.get(triple.getObject());
                }

                loops++;
                for (Long c1a : c1as) {

                    if (c1a == triple.getSubject() && triple.getObject() != triple.getSubject()) {

                        Triple result = new ImmutableTriple(triple.getSubject(), equivalentProperty, triple.getObject());
                        outputTriples.add(result);

                        logTrace(dictionary.printTriple(new ImmutableTriple(triple.getSubject(), subPropertyOf, triple.getObject())) + " & " + dictionary.printTriple(new ImmutableTriple(triple.getObject(), subPropertyOf, triple.getSubject())) + " -> " + dictionary.printTriple(result));
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
