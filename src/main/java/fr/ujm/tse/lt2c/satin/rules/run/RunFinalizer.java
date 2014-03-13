package fr.ujm.tse.lt2c.satin.rules.run;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.buffer.QueuedTripleBufferLock;
import fr.ujm.tse.lt2c.satin.dictionary.AbstractDictionary;
import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.rules.ReasonerProfile;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;

/**
 * INPUT
 * c1 rdfs:subClassOf c2
 * x rdf:type c1
 * OUPUT
 * x rdf:type c2
 */
public class RunFinalizer implements BufferListener {

    private static final Logger logger = Logger.getLogger(RunFinalizer.class);
    private final TripleStore tripleStore;
    private final Dictionary dictionary;
    private final TripleBuffer tripleBuffer;
    private final ExecutorService executor;
    private final AtomicInteger phaser;
    private final ReasonerProfile profile;
    private final boolean useful;

    public RunFinalizer(final TripleStore tripleStore, final Dictionary dictionary, final ReasonerProfile profile, final ExecutorService executor,
            final AtomicInteger phaser, final int bufferSize) {
        this.tripleStore = tripleStore;
        this.dictionary = dictionary;
        this.executor = executor;
        this.phaser = phaser;
        this.tripleBuffer = new QueuedTripleBufferLock(bufferSize);
        this.tripleBuffer.addBufferListener(this);
        this.profile = profile;
        switch (profile) {
        case RhoDFPP:
            this.useful = true;
            break;
        default:
            this.useful = false;
            break;
        }
    }

    public void addTriples(final Collection<Triple> triples) {
        this.tripleBuffer.addAll(triples);
    }

    @Override
    public boolean bufferFull() {
        switch (this.profile) {
        case RhoDFPP:
            return this.bufferFullRhoDFPP();
        default:
            return true;
        }
    }

    public boolean bufferFullRhoDFPP() {
        final Runnable run = new Runnable() {
            @Override
            public void run() {

                final long subPropertyOf = AbstractDictionary.subPropertyOf;
                final long subClassOf = AbstractDictionary.subClassOf;
                final long domain = AbstractDictionary.domain;
                final long range = AbstractDictionary.range;
                final long type = AbstractDictionary.type;

                final Collection<Triple> outputTriples = new HashSet<>();

                for (final Triple triple : RunFinalizer.this.tripleBuffer.clear().getAll()) {
                    final long s = triple.getSubject(), p = triple.getPredicate(), o = triple.getObject();

                    /* subproperty reflexivity */

                    /* x sp x (x in RhoDF) */
                    outputTriples.add(new ImmutableTriple(subPropertyOf, subPropertyOf, subPropertyOf));
                    outputTriples.add(new ImmutableTriple(subClassOf, subPropertyOf, subClassOf));
                    outputTriples.add(new ImmutableTriple(domain, subPropertyOf, domain));
                    outputTriples.add(new ImmutableTriple(range, subPropertyOf, range));
                    outputTriples.add(new ImmutableTriple(type, subPropertyOf, type));

                    /* s p o -> p sp p */
                    outputTriples.add(new ImmutableTriple(p, subPropertyOf, p));

                    /* s sp o -> s sp s & o sp o */
                    if (p == subPropertyOf) {
                        outputTriples.add(new ImmutableTriple(s, subPropertyOf, s));
                        if (s != o) {
                            outputTriples.add(new ImmutableTriple(o, subPropertyOf, o));
                        }
                    }

                    /* s p o -> s sp s (p in {domain, range} */
                    if ((p == domain) || (p == range)) {
                        outputTriples.add(new ImmutableTriple(s, subPropertyOf, s));
                    }

                    /* subclass reflexivity */

                    /* s sc o -> s sc s & o sc o */
                    if (p == subClassOf) {
                        outputTriples.add(new ImmutableTriple(s, subClassOf, s));
                        outputTriples.add(new ImmutableTriple(o, subClassOf, o));
                    }

                    /* s p o -> o sc o (p in {domain, range, type} */
                    if ((p == domain) || (p == range) || (p == type)) {
                        outputTriples.add(new ImmutableTriple(o, subClassOf, o));
                    }
                }
                for (final Triple triple : outputTriples) {
                    if (!RunFinalizer.this.tripleStore.contains(triple)) {
                        RunFinalizer.this.tripleStore.add(triple);
                    } else {
                        if (logger.isTraceEnabled()) {
                            logger.trace(RunFinalizer.this.dictionary.printTriple(triple) + " already present");
                        }
                    }
                }
                synchronized (RunFinalizer.this.phaser) {
                    RunFinalizer.this.phaser.decrementAndGet();
                    RunFinalizer.this.phaser.notifyAll();
                }
            }
        };

        RunFinalizer.this.phaser.incrementAndGet();
        this.executor.submit(run);
        return true;
    }

    public boolean clearBuffer() {
        return this.bufferFull();
    }

    public long size() {
        return this.tripleBuffer.getOccupation();
    }

    public Logger getLogger() {
        return logger;
    }

    public boolean isUseful() {
        return this.useful;
    }

    @Override
    public String toString() {
        return "RunFinalzer";
    }

}
