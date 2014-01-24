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
 * p2 rdfs:domain c
 * p1 rdfs:subPropertyOf p2
 * OUPUT
 * p1 rdfs:domain c
 */
public class RunSCM_DOM2 extends AbstractRun {

    private static final Logger logger = Logger.getLogger(RunSCM_DOM2.class);
    public static final long[] INPUT_MATCHERS = { AbstractDictionary.domain, AbstractDictionary.subPropertyOf };
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.domain };
    public static final String ruleName = "SCM_DOM2";

    public RunSCM_DOM2(final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer, final TripleDistributor tripleDistributor, final AtomicInteger phaser) {
        super(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);

    }

    @Override
    protected int process(final TripleStore ts1, final TripleStore ts2, final Collection<Triple> outputTriples) {

        final long subPropertyOf = AbstractDictionary.subPropertyOf;
        final long domain = AbstractDictionary.domain;

        int loops = 0;

        final Multimap<Long, Long> domainMultimap = ts1.getMultiMapForPredicate(domain);
        if ((domainMultimap != null) && !domainMultimap.isEmpty()) {

            final Collection<Triple> subpropertyTriples = ts2.getbyPredicate(subPropertyOf);

            final HashMap<Long, Collection<Long>> cachePredicates = new HashMap<>();

            /* For each type triple */
            for (final Triple triple : subpropertyTriples) {
                /*
                 * Get all objects (c2) of subClassOf triples with domain
                 * triples objects as subject
                 */

                Collection<Long> cs;
                if (!cachePredicates.containsKey(triple.getObject())) {
                    cs = domainMultimap.get(triple.getObject());
                    cachePredicates.put(triple.getObject(), cs);
                } else {
                    cs = cachePredicates.get(triple.getObject());
                }

                loops++;
                for (final Long c : cs) {

                    final Triple result = new ImmutableTriple(triple.getSubject(), domain, c);
                    outputTriples.add(result);

                    logTrace(dictionary.printTriple(new ImmutableTriple(triple.getSubject(), subPropertyOf, triple.getObject())) + " & " + dictionary.printTriple(new ImmutableTriple(triple.getObject(), domain, c)) + " -> " + dictionary.printTriple(result));
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
