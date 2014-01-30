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
 * p rdfs:domain c1
 * c1 rdfs:subClassOf c2
 * OUPUT
 * p rdfs:domain c2
 */
public class RunSCM_DOM1 extends AbstractRun {

    private static final Logger logger = Logger.getLogger(RunSCM_DOM1.class);
    public static final long[] INPUT_MATCHERS = { AbstractDictionary.domain, AbstractDictionary.subClassOf };
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.domain };
    public static final String ruleName = "SCM_DOM1";

    public RunSCM_DOM1(final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer, final TripleDistributor tripleDistributor, final AtomicInteger phaser) {
        super(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);

    }

    @Override
    protected int process(final TripleStore ts1, final TripleStore ts2, final Collection<Triple> outputTriples) {

        final long domain = AbstractDictionary.domain;
        final long subClassOf = AbstractDictionary.subClassOf;

        int loops = 0;

        final Multimap<Long, Long> subclassMultimap = ts1.getMultiMapForPredicate(subClassOf);
        if ((subclassMultimap != null) && !subclassMultimap.isEmpty()) {

            final HashMap<Long, Collection<Long>> cachePredicates = new HashMap<>();

            final Collection<Triple> domainTriples = ts2.getbyPredicate(domain);

            /* For each type triple */
            for (final Triple triple : domainTriples) {
                /*
                 * Get all objects (c2) of subClassOf triples with domain
                 * triples objects as subject
                 */

                Collection<Long> c2s;
                if (!cachePredicates.containsKey(triple.getObject())) {
                    c2s = subclassMultimap.get(triple.getObject());
                    cachePredicates.put(triple.getObject(), c2s);
                } else {
                    c2s = cachePredicates.get(triple.getObject());
                }

                loops++;
                for (final Long c2 : c2s) {

                    final Triple result = new ImmutableTriple(triple.getSubject(), domain, c2);
                    outputTriples.add(result);

                    if (logger.isTraceEnabled()) {
                        logger.trace(dictionary.printTriple(new ImmutableTriple(triple.getSubject(), domain, triple.getObject())) + " & " + dictionary.printTriple(new ImmutableTriple(triple.getObject(), subClassOf, c2)) + " -> " + dictionary.printTriple(result));
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