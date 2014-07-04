package fr.ujm.tse.lt2c.satin.rules.run;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.buffer.BufferTimer;
import fr.ujm.tse.lt2c.satin.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.dictionary.AbstractDictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;

/**
 * INPUT
 * x p y
 * OUPUT
 * x rdf:type rdfs:Ressource
 * y rdf:type rdfs:Ressource
 */
public class RunRDFS4 extends AbstractRun {

    private static final Logger LOGGER = Logger.getLogger(RunRDFS4.class);
    private static final String RULENAME = "RDFS4";
    public static final long[] INPUT_MATCHERS = {};
    public static final long[] OUTPUT_MATCHERS = { AbstractDictionary.type };

    public RunRDFS4(final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer, final TripleDistributor tripleDistributor,
            final AtomicInteger phaser) {
        super(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        super.complexity = 1;
    }

    @Override
    protected int process(final TripleStore ts1, final TripleStore ts2, final Collection<Triple> outputTriples) {

        final long ressouce = AbstractDictionary.ressource;
        final long type = AbstractDictionary.type;

        int loops = 0;

        for (final Triple triple : ts2.getAll()) {
            loops++;
            final Triple result1 = new ImmutableTriple(triple.getSubject(), type, ressouce);
            final Triple result2 = new ImmutableTriple(triple.getObject(), type, ressouce);
            outputTriples.add(result1);
            outputTriples.add(result2);
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
