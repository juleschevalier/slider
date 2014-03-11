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
 * c1 rdfs:subClassOf c2
 * c2 rdfs:subClassOf c1
 * OUPUT
 * c1 owl:equivalentClass c2
 */
public class RunSCM_EQC2 extends AbstractRun {

    private static final Logger logger = Logger.getLogger(RunSCM_EQC2.class);
    public static final long[] INPUT_MATCHERS = { AbstractDictionary.subClassOf };
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.equivalentClass };
    public static final String ruleName = "SCM_EQC2";

    public RunSCM_EQC2(final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer, final TripleDistributor tripleDistributor,
            final AtomicInteger phaser) {
        super(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);

    }

    @Override
    protected int process(final TripleStore ts1, final TripleStore ts2, final Collection<Triple> outputTriples) {

        final long subClassOf = AbstractDictionary.subClassOf;
        final long equivalentClass = AbstractDictionary.equivalentClass;

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

                Collection<Long> c1as;
                if (!cachePredicates.containsKey(triple.getObject())) {
                    c1as = subclassMultimap.get(triple.getObject());
                    cachePredicates.put(triple.getObject(), c1as);
                } else {
                    c1as = cachePredicates.get(triple.getObject());
                }

                loops++;
                for (final Long c1a : c1as) {

                    if ((c1a == triple.getSubject()) && (triple.getObject() != triple.getSubject())) {

                        final Triple result = new ImmutableTriple(triple.getSubject(), equivalentClass, triple.getObject());
                        outputTriples.add(result);
                        // if (logger.isTraceEnabled()) {
                        // logger.trace(dictionary.printTriple(new ImmutableTriple(triple.getSubject(), subClassOf,
                        // triple.getObject())) + " & " + dictionary.printTriple(new ImmutableTriple(triple.getObject(),
                        // subClassOf, triple.getSubject())) + " -> " + dictionary.printTriple(result));
                        // }
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
