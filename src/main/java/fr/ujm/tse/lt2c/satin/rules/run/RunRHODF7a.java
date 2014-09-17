package fr.ujm.tse.lt2c.satin.rules.run;

import java.util.Collection;
import java.util.Map.Entry;
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
 * OUPUT
 * c1 rdfs:subClassOf c1
 * c2 rdfs:subClassOf c2
 */
public class RunRHODF7a extends AbstractRun {

    private static final Logger LOGGER = Logger.getLogger(RunRHODF7a.class);
    private static final String RULENAME = "RHODF7a";
    public static final long[] INPUT_MATCHERS = { AbstractDictionary.subClassOf };
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.subClassOf };

    public RunRHODF7a(final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer, final TripleDistributor tripleDistributor,
            final AtomicInteger phaser) {
        super(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        super.ruleName = RULENAME;
        super.complexity = 1;
    }

    @Override
    protected int process(final TripleStore ts1, final TripleStore ts2, final Collection<Triple> outputTriples) {

        final long subClassOf = AbstractDictionary.subClassOf;

        int loops = 0;

        final Multimap<Long, Long> subpropertyMultiMap = ts2.getMultiMapForPredicate(subClassOf);
        if (subpropertyMultiMap != null && !subpropertyMultiMap.isEmpty()) {
            for (final Entry<Long, Long> entry : subpropertyMultiMap.entries()) {
                loops++;
                final Triple result1 = new ImmutableTriple(entry.getKey(), subClassOf, entry.getKey());
                final Triple result2 = new ImmutableTriple(entry.getValue(), subClassOf, entry.getValue());
                outputTriples.add(result1);
                outputTriples.add(result2);
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
