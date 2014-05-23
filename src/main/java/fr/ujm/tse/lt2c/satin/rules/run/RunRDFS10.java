package fr.ujm.tse.lt2c.satin.rules.run;

import java.util.Collection;
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
 * x rdf:type rdfs:Class
 * OUPUT
 * x rdf:subClassOf x
 */
public class RunRDFS10 extends AbstractRun {

    private static final Logger LOGGER = Logger.getLogger(RunRDFS10.class);
    private static final String RULENAME = "RDFS6";
    public static final long[] INPUT_MATCHERS = { AbstractDictionary.type };
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.type };

    public RunRDFS10(final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer, final TripleDistributor tripleDistributor,
            final AtomicInteger phaser) {
        super(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        super.complexity = 1;
    }

    @Override
    protected int process(final TripleStore ts1, final TripleStore ts2, final Collection<Triple> outputTriples) {

        final long subClassOf = AbstractDictionary.subClassOf;
        final long type = AbstractDictionary.type;
        final long clazz = AbstractDictionary.classRdfs;

        int loops = 0;

        final Multimap<Long, Long> typeMultimap = ts1.getMultiMapForPredicate(type);
        if (typeMultimap != null && !typeMultimap.isEmpty()) {
            for (final Long subject : typeMultimap.keySet()) {
                if (typeMultimap.get(subject).contains(clazz)) {
                    loops++;
                    final Triple result = new ImmutableTriple(subject, subClassOf, subject);
                    outputTriples.add(result);
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
