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
 * INPUT p3 rdfs:subPropertyOf p2 p2 rdfs:subPropertyOf p3 OUPUT p3
 * rdfs:subPropertyOf p3
 */
public class RunSCM_SPO extends AbstractRun {

    private static final Logger logger = Logger.getLogger(RunSCM_SPO.class);
    public static final long[] INPUT_MATCHERS = { AbstractDictionary.subPropertyOf };
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.subPropertyOf };
    public static final String ruleName = "SCM_SPO";

    public RunSCM_SPO(final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer, final TripleDistributor tripleDistributor, final AtomicInteger phaser) {
        super(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);

    }

    @Override
    protected int process(final TripleStore ts1, final TripleStore ts2, final Collection<Triple> outputTriples) {

        final long subPropertyOf = AbstractDictionary.subPropertyOf;

        int loops = 0;

        final Multimap<Long, Long> subpropertyMultimap = ts1.getMultiMapForPredicate(subPropertyOf);
        if ((subpropertyMultimap != null) && !subpropertyMultimap.isEmpty()) {

            final Collection<Triple> subpropertyTriples = ts2.getbyPredicate(subPropertyOf);

            final HashMap<Long, Collection<Long>> cachePredicates = new HashMap<>();

            /* For each type triple */
            for (final Triple triple : subpropertyTriples) {
                /*
                 * Get all objects (p3) of subPropertyOf triples with
                 */

                Collection<Long> p3s;
                if (!cachePredicates.containsKey(triple.getObject())) {
                    p3s = subpropertyMultimap.get(triple.getObject());
                    cachePredicates.put(triple.getObject(), p3s);
                } else {
                    p3s = cachePredicates.get(triple.getObject());
                }

                loops++;
                for (final Long p3 : p3s) {

                    if (p3 != triple.getSubject()) {

                        final Triple result = new ImmutableTriple(triple.getSubject(), subPropertyOf, p3);
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
    public String toString() {
        return this.ruleName;
    }

}
