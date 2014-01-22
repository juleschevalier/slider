package fr.ujm.tse.lt2c.satin.rules.run;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Phaser;

import org.apache.log4j.Logger;

import com.google.common.collect.Multimap;

import fr.ujm.tse.lt2c.satin.dictionary.AbstractDictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;

/**
 * INPUT p rdfs:domain c x p y OUPUT x rdf:type c
 */
public class RunPRP_DOM extends AbstractRun {

    private static final Logger logger = Logger.getLogger(RunPRP_DOM.class);
    public static final long[] INPUT_MATCHERS = {};
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.type };

    public RunPRP_DOM(Dictionary dictionary, TripleStore tripleStore, Phaser phaser) {
        super(dictionary, tripleStore, phaser, "PRP_DOM");

    }

    @Override
    protected int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples) {

        long domain = AbstractDictionary.domain;
        long type = AbstractDictionary.type;

        int loops = 0;

        Multimap<Long, Long> domainMultiMap = ts1.getMultiMapForPredicate(domain);
        if (domainMultiMap != null && !domainMultiMap.isEmpty()) {

            HashMap<Long, Collection<Triple>> cachePredicates = new HashMap<>();

            for (Long p : domainMultiMap.keySet()) {

                Collection<Triple> matchingTriples;
                if (!cachePredicates.containsKey(p)) {
                    matchingTriples = ts2.getbyPredicate(p);
                    cachePredicates.put(p, matchingTriples);
                } else {
                    matchingTriples = cachePredicates.get(p);
                }

                for (Triple triple : matchingTriples) {

                    for (Long c : domainMultiMap.get(p)) {

                        if (triple.getSubject() >= 0) {
                            Triple result = new ImmutableTriple(triple.getSubject(), type, c);
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
    public long[] getInputMatchers() {
        return INPUT_MATCHERS;
    }

    @Override
    public long[] getOutputMatchers() {
        return OUTPUT_MATCHERS;
    }

}