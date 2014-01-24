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
 * INPUT p rdfs:domain c x p y OUPUT x rdf:type c
 */
public class RunPRP_DOM extends AbstractRun {

    private static final Logger logger = Logger.getLogger(RunPRP_DOM.class);
    public static final long[] INPUT_MATCHERS = {};
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.type };
    public static final String ruleName = "PRP_DOM";

    public RunPRP_DOM(final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer, final TripleDistributor tripleDistributor, final AtomicInteger phaser) {
        super(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);

    }

    @Override
    protected int process(final TripleStore ts1, final TripleStore ts2, final Collection<Triple> outputTriples) {

        final long domain = AbstractDictionary.domain;
        final long type = AbstractDictionary.type;

        final int loops = 0;

        final Multimap<Long, Long> domainMultiMap = ts1.getMultiMapForPredicate(domain);
        if ((domainMultiMap != null) && !domainMultiMap.isEmpty()) {

            final HashMap<Long, Collection<Triple>> cachePredicates = new HashMap<>();

            for (final Long p : domainMultiMap.keySet()) {

                Collection<Triple> matchingTriples;
                if (!cachePredicates.containsKey(p)) {
                    matchingTriples = ts2.getbyPredicate(p);
                    cachePredicates.put(p, matchingTriples);
                } else {
                    matchingTriples = cachePredicates.get(p);
                }

                for (final Triple triple : matchingTriples) {

                    for (final Long c : domainMultiMap.get(p)) {

                        if (triple.getSubject() >= 0) {
                            final Triple result = new ImmutableTriple(triple.getSubject(), type, c);
                            logTrace(dictionary.printTriple(triple) + " & " + dictionary.printTriple(new ImmutableTriple(p, domain, c)) + " -> " + dictionary.printTriple(result));
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