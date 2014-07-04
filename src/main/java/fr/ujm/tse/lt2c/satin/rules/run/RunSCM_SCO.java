package fr.ujm.tse.lt2c.satin.rules.run;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.google.common.collect.Multimap;

import fr.ujm.tse.lt2c.satin.buffer.BufferTimer;
import fr.ujm.tse.lt2c.satin.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.dictionary.AbstractDictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;

/**
 * INPUT
 * c1 rdfs:subClassOf c2
 * c2 rdfs:subClassOf c3
 * OUPUT
 * c1 rdfs:subClassOf c3
 */
public class RunSCM_SCO extends AbstractRun {

    private static final Logger LOGGER = Logger.getLogger(RunSCM_SCO.class);
    private static final String RULE_NAME = "SCM_SCO";
    public static final long[] INPUT_MATCHERS = { AbstractDictionary.subClassOf };
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.subClassOf };

    public RunSCM_SCO(final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer, final TripleDistributor tripleDistributor,
            final AtomicInteger phaser) {
        super(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);

    }

    @Override
    protected int process(final TripleStore ts1, final TripleStore ts2, final Collection<Triple> outputTriples) {

        final long subClassOf = AbstractDictionary.subClassOf;

        int loops = 0;

        final Multimap<Long, Long> subclassMultimap = ts1.getMultiMapForPredicate(subClassOf);
        if ((subclassMultimap != null) && !subclassMultimap.isEmpty()) {

            final Collection<Triple> subclassTriples = ts2.getbyPredicate(subClassOf);

            final HashMap<Long, Collection<Long>> cachePredicates = new HashMap<>();

            /* For each type triple */
            for (final Triple triple : subclassTriples) {
                /*
                 * Get all objects (c1a) of subClassOf triples with
                 */

                Collection<Long> c3s;
                if (!cachePredicates.containsKey(triple.getObject())) {
                    c3s = subclassMultimap.get(triple.getObject());
                    cachePredicates.put(triple.getObject(), c3s);
                } else {
                    c3s = cachePredicates.get(triple.getObject());
                }

                loops++;
                for (final Long c1a : c3s) {

                    if ((c1a != triple.getSubject()) && !ts1.contains(triple.getSubject(), subClassOf, c1a)
                            && !ts2.contains(triple.getSubject(), subClassOf, c1a)) {
                        final Triple result = new ImmutableTriple(triple.getSubject(), subClassOf, c1a);
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
        return RULE_NAME;
    }

}
