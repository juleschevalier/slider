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

    public RunSCM_DOM1(Dictionary dictionary, TripleStore tripleStore, Phaser phaser) {
        super(dictionary, tripleStore, phaser, "SCM_DOM1");

    }

    @Override
    protected int process(TripleStore ts1, TripleStore ts2, Collection<Triple> outputTriples) {

        long domain = AbstractDictionary.domain;
        long subClassOf = AbstractDictionary.subClassOf;

        int loops = 0;

        Multimap<Long, Long> subclassMultimap = ts1.getMultiMapForPredicate(subClassOf);
        if (subclassMultimap != null && !subclassMultimap.isEmpty()) {

            HashMap<Long, Collection<Long>> cachePredicates = new HashMap<>();

            Collection<Triple> domainTriples = ts2.getbyPredicate(domain);

            /* For each type triple */
            for (Triple triple : domainTriples) {
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
                for (Long c2 : c2s) {

                    Triple result = new ImmutableTriple(triple.getSubject(), domain, c2);
                    outputTriples.add(result);

                    logTrace(dictionary.printTriple(new ImmutableTriple(triple.getSubject(), domain, triple.getObject())) + " & " + dictionary.printTriple(new ImmutableTriple(triple.getObject(), subClassOf, c2)) + " -> " + dictionary.printTriple(result));
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