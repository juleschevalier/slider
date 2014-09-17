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
 * c1 rdfs:subClassOf c2
 * x rdf:type c1
 * OUPUT
 * x rdf:type c2
 */
public class RunCAX_SCO extends AbstractRun {

    private static final Logger LOGGER = Logger.getLogger(RunCAX_SCO.class);
    private static final String RULENAME = "CAX_SCO";
    public static final long[] INPUT_MATCHERS = { AbstractDictionary.subClassOf, AbstractDictionary.type };
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.type };

    public RunCAX_SCO(final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer, final TripleDistributor tripleDistributor,
            final AtomicInteger phaser) {
        super(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        super.ruleName = RULENAME;
    }

    @Override
    protected int process(final TripleStore ts1, final TripleStore ts2, final Collection<Triple> outputTriples) {

        final long subClassOf = AbstractDictionary.subClassOf;
        final long type = AbstractDictionary.type;

        int loops = 0;

        final Multimap<Long, Long> subclassMultimap = ts1.getMultiMapForPredicate(subClassOf);
        if (subclassMultimap != null && !subclassMultimap.isEmpty()) {

            final HashMap<Long, Collection<Long>> cachePredicates = new HashMap<>();

            final Collection<Triple> types = ts2.getbyPredicate(type);
            for (final Triple typeTriple : types) {

                Collection<Long> c2s;
                if (!cachePredicates.containsKey(typeTriple.getObject())) {
                    c2s = subclassMultimap.get(typeTriple.getObject());
                    cachePredicates.put(typeTriple.getObject(), c2s);
                } else {
                    c2s = cachePredicates.get(typeTriple.getObject());
                }

                loops++;
                for (final Long c2 : c2s) {

                    if (typeTriple.getSubject() >= 0) {
                        final Triple result = new ImmutableTriple(typeTriple.getSubject(), type, c2);
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
        return RULENAME;
    }

}
