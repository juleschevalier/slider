package fr.ujm.tse.lt2c.satin.rules.run;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.google.common.collect.Multimap;

import fr.ujm.tse.lt2c.satin.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.dictionary.AbstractDictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;

/**
 * INPUT
 * p rdfs:range c
 * x p y
 * OUPUT
 * y rdf:type c
 */
public class RunPRP_RNG extends AbstractRun {

    private static final Logger logger = Logger.getLogger(RunPRP_RNG.class);
    public static final long[] INPUT_MATCHERS = {};
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.type };
    public static final String ruleName = "PRP_RNG";

    public RunPRP_RNG(final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer, final TripleDistributor tripleDistributor, final AtomicInteger phaser) {
        super(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);

    }

    @Override
    protected int process(final TripleStore ts1, final TripleStore ts2, final Collection<Triple> outputTriples) {

        final long range = AbstractDictionary.range;
        final long type = AbstractDictionary.type;

        final int loops = 0;

        final Multimap<Long, Long> rangeMultiMap = ts1.getMultiMapForPredicate(range);
        if ((rangeMultiMap != null) && !rangeMultiMap.isEmpty()) {

            final HashMap<Long, Collection<Triple>> cachePredicates = new HashMap<>();

            for (final Long p : rangeMultiMap.keySet()) {

                Collection<Triple> matchingTriples;
                if (!cachePredicates.containsKey(p)) {
                    matchingTriples = ts2.getbyPredicate(p);
                    cachePredicates.put(p, matchingTriples);
                } else {
                    matchingTriples = cachePredicates.get(p);
                }

                for (final Triple triple : matchingTriples) {

                    for (final Long c : rangeMultiMap.get(p)) {

                        if (triple.getObject() >= 0) {
                            final Triple result = new ImmutableTriple(triple.getObject(), type, c);
                            if (logger.isTraceEnabled()) {
                                logger.trace(dictionary.printTriple(triple) + " & " + dictionary.printTriple(new ImmutableTriple(p, range, c)) + " -> " + dictionary.printTriple(result));
                            }
                            outputTriples.add(result);
                        }
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
    public String toString() {
        return this.ruleName;
    }

}
