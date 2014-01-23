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
 * p2 rdfs:range c
 * p1 rdfs:subPropertyOf p2
 * OUPUT
 * p1 rdfs:range c
 */
public class RunSCM_RNG2 extends AbstractRun {

    private static final Logger logger = Logger.getLogger(RunSCM_RNG2.class);
    public static final long[] INPUT_MATCHERS = { AbstractDictionary.range, AbstractDictionary.subPropertyOf };
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.range };

    public RunSCM_RNG2(Dictionary dictionary, TripleStore tripleStore, AtomicInteger phaser) {
        super(dictionary, tripleStore, phaser, "SCM_RNG2");

    }

    @Override
    protected int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples) {

        long subPropertyOf = AbstractDictionary.subPropertyOf;
        long range = AbstractDictionary.range;

        int loops = 0;

        Multimap<Long, Long> rangeMultimap = ts1.getMultiMapForPredicate(range);
        if (rangeMultimap != null && !rangeMultimap.isEmpty()) {

            Collection<Triple> subpropertyTriples = ts2.getbyPredicate(subPropertyOf);

            HashMap<Long, Collection<Long>> cachePredicates = new HashMap<>();

            /* For each type triple */
            for (Triple triple : subpropertyTriples) {
                /*
                 * Get all objects (c2) of subClassOf triples with range triples
                 * objects as subject
                 */

                Collection<Long> cs;
                if (!cachePredicates.containsKey(triple.getObject())) {
                    cs = rangeMultimap.get(triple.getObject());
                    cachePredicates.put(triple.getObject(), cs);
                } else {
                    cs = cachePredicates.get(triple.getObject());
                }

                loops++;
                for (Long c : cs) {

                    Triple result = new ImmutableTriple(triple.getSubject(), range, c);
                    outputTriples.add(result);

                    logTrace(dictionary.printTriple(new ImmutableTriple(triple.getSubject(), subPropertyOf, triple.getObject())) + " & " + dictionary.printTriple(new ImmutableTriple(triple.getObject(), range, c)) + " -> " + dictionary.printTriple(result));
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
