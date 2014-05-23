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
 * p1 rdfs:subPropertyOf p2
 * x p1 y
 * OUPUT
 * x p2 y
 */
public class RunPRP_SPO1 extends AbstractRun {

    private static final Logger LOGGER = Logger.getLogger(RunPRP_SPO1.class);
    private static final String RULENAME = "PRP_SPO1";
    public static final long[] INPUT_MATCHERS = {};
    public static final long[] OUTPUT_MATCHERS = {};

    public RunPRP_SPO1(final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer, final TripleDistributor tripleDistributor,
            final AtomicInteger phaser) {
        super(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);

    }

    @Override
    protected int process(final TripleStore ts1, final TripleStore ts2, final Collection<Triple> outputTriples) {

        final long subPropertyOf = AbstractDictionary.subPropertyOf;

        final int loops = 0;

        final Multimap<Long, Long> subPropertyOfMultiMap = ts1.getMultiMapForPredicate(subPropertyOf);
        if ((subPropertyOfMultiMap != null) && !subPropertyOfMultiMap.isEmpty()) {

            final HashMap<Long, Collection<Triple>> cachePredicates = new HashMap<>();

            for (final Long p1 : subPropertyOfMultiMap.keySet()) {

                Collection<Triple> matchingTriples;
                if (!cachePredicates.containsKey(p1)) {
                    matchingTriples = ts2.getbyPredicate(p1);
                    cachePredicates.put(p1, matchingTriples);
                } else {
                    matchingTriples = cachePredicates.get(p1);
                }

                for (final Triple triple : matchingTriples) {

                    for (final Long p2 : subPropertyOfMultiMap.get(p1)) {

                        final Triple result = new ImmutableTriple(triple.getSubject(), p2, triple.getObject());
                        outputTriples.add(result);
                    }

                }
            }
        }

        return loops;

    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public String toString() {
        return this.RULENAME;
    }

}
