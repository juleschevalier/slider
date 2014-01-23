/**
 * 
 */
package fr.ujm.tse.lt2c.satin.rules.test;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import fr.ujm.tse.lt2c.satin.dictionary.AbstractDictionary;
import fr.ujm.tse.lt2c.satin.dictionary.DictionaryPrimitrivesRWLock;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.rules.run.RunCAX_SCO;
import fr.ujm.tse.lt2c.satin.rules.run.RunPRP_DOM;
import fr.ujm.tse.lt2c.satin.rules.run.RunPRP_RNG;
import fr.ujm.tse.lt2c.satin.rules.run.RunPRP_SPO1;
import fr.ujm.tse.lt2c.satin.rules.run.RunSCM_DOM1;
import fr.ujm.tse.lt2c.satin.rules.run.RunSCM_DOM2;
import fr.ujm.tse.lt2c.satin.rules.run.RunSCM_EQC2;
import fr.ujm.tse.lt2c.satin.rules.run.RunSCM_EQP2;
import fr.ujm.tse.lt2c.satin.rules.run.RunSCM_RNG1;
import fr.ujm.tse.lt2c.satin.rules.run.RunSCM_RNG2;
import fr.ujm.tse.lt2c.satin.rules.run.RunSCM_SCO;
import fr.ujm.tse.lt2c.satin.rules.run.RunSCM_SPO;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

/**
 * @author Jules Chevalier
 * 
 */
public class TestIndividualRules {

    @Test
    public void testCAX_SCO() {
        /*
         * Initialization
         */
        Dictionary dictionary = new DictionaryPrimitrivesRWLock();
        TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        RunCAX_SCO runCAX_SCO = new RunCAX_SCO(dictionary, tripleStore, new AtomicInteger());

        /*
         * Test with matching triples
         */
        long c1 = dictionary.add("c1"), c2 = dictionary.add("c2"), x = dictionary.add("x");

        Triple t1 = new ImmutableTriple(c1, AbstractDictionary.subClassOf, c2), t2 = new ImmutableTriple(x, AbstractDictionary.type, c1);

        tripleStore.add(t1);
        tripleStore.add(t2);

        runCAX_SCO.getTripleBuffer().add(t1);
        runCAX_SCO.getTripleBuffer().add(t2);

        Assert.assertEquals(tripleStore.size(), 2);

        runCAX_SCO.run();

        Assert.assertEquals(tripleStore.size(), 3);

        /*
         * Test with non-matching triples
         */

        long a = dictionary.add("a"), b = dictionary.add("b");
        Triple t3 = new ImmutableTriple(a, AbstractDictionary.type, b);

        tripleStore.add(t3);
        runCAX_SCO.getTripleBuffer().add(t3);

        runCAX_SCO.run();

        Assert.assertEquals(tripleStore.size(), 4);

    }

    @Test
    public void testPRP_DOM() {
        Dictionary dictionary = new DictionaryPrimitrivesRWLock();
        TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        RunPRP_DOM runPRP_DOM = new RunPRP_DOM(dictionary, tripleStore, new AtomicInteger());

        /*
         * Test with matching triples
         */
        long x = dictionary.add("x"), y = dictionary.add("y"), p = dictionary.add("p"), c = dictionary.add("c");

        Triple t1 = new ImmutableTriple(p, AbstractDictionary.domain, c), t2 = new ImmutableTriple(x, p, y);

        tripleStore.add(t1);
        tripleStore.add(t2);

        runPRP_DOM.getTripleBuffer().add(t1);
        runPRP_DOM.getTripleBuffer().add(t2);

        Assert.assertEquals(tripleStore.size(), 2);

        runPRP_DOM.run();

        Assert.assertEquals(tripleStore.size(), 3);

        /*
         * Test with non-matching triples
         */

        long a = dictionary.add("a"), b = dictionary.add("b");
        Triple t3 = new ImmutableTriple(a, AbstractDictionary.domain, b);

        tripleStore.add(t3);
        runPRP_DOM.getTripleBuffer().add(t3);

        runPRP_DOM.run();

        Assert.assertEquals(tripleStore.size(), 4);

    }

    @Test
    public void testPRP_RNG() {
        Dictionary dictionary = new DictionaryPrimitrivesRWLock();
        TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        RunPRP_RNG runPRP_RNG = new RunPRP_RNG(dictionary, tripleStore, new AtomicInteger());

        /*
         * Test with matching triples
         */
        long x = dictionary.add("x"), y = dictionary.add("y"), p = dictionary.add("p"), c = dictionary.add("c");

        Triple t1 = new ImmutableTriple(p, AbstractDictionary.range, c), t2 = new ImmutableTriple(x, p, y);

        tripleStore.add(t1);
        tripleStore.add(t2);

        runPRP_RNG.getTripleBuffer().add(t1);
        runPRP_RNG.getTripleBuffer().add(t2);

        Assert.assertEquals(tripleStore.size(), 2);

        runPRP_RNG.run();

        Assert.assertEquals(tripleStore.size(), 3);

        /*
         * Test with non-matching triples
         */

        long a = dictionary.add("a"), b = dictionary.add("b");
        Triple t3 = new ImmutableTriple(a, AbstractDictionary.range, b);

        tripleStore.add(t3);
        runPRP_RNG.getTripleBuffer().add(t3);

        runPRP_RNG.run();

        Assert.assertEquals(tripleStore.size(), 4);

    }

    @Test
    public void testSCM_SCO() {
        Dictionary dictionary = new DictionaryPrimitrivesRWLock();
        TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        RunSCM_SCO runSCM_SCO = new RunSCM_SCO(dictionary, tripleStore, new AtomicInteger());

        /*
         * Test with matching triples
         */
        long c1 = dictionary.add("c1"), c2 = dictionary.add("c2"), c3 = dictionary.add("c3");

        Triple t1 = new ImmutableTriple(c1, AbstractDictionary.subClassOf, c2), t2 = new ImmutableTriple(c2, AbstractDictionary.subClassOf, c3);

        tripleStore.add(t1);
        tripleStore.add(t2);

        runSCM_SCO.getTripleBuffer().add(t1);
        runSCM_SCO.getTripleBuffer().add(t2);

        Assert.assertEquals(tripleStore.size(), 2);

        runSCM_SCO.run();

        Assert.assertEquals(tripleStore.size(), 3);

        /*
         * Test with non-matching triples
         */

        long a = dictionary.add("a"), b = dictionary.add("b");
        Triple t3 = new ImmutableTriple(a, AbstractDictionary.subClassOf, b);

        tripleStore.add(t3);
        runSCM_SCO.getTripleBuffer().add(t3);

        runSCM_SCO.run();

        Assert.assertEquals(tripleStore.size(), 4);

    }

    @Test
    public void testPRP_SPO1() {
        Dictionary dictionary = new DictionaryPrimitrivesRWLock();
        TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        RunPRP_SPO1 runPRP_SPO1 = new RunPRP_SPO1(dictionary, tripleStore, new AtomicInteger());

    }

    @Test
    public void testSCM_DOM1() {
        Dictionary dictionary = new DictionaryPrimitrivesRWLock();
        TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        RunSCM_DOM1 runSCM_DOM1 = new RunSCM_DOM1(dictionary, tripleStore, new AtomicInteger());

    }

    @Test
    public void testSCM_DOM2() {
        Dictionary dictionary = new DictionaryPrimitrivesRWLock();
        TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        RunSCM_DOM2 runSCM_DOM2 = new RunSCM_DOM2(dictionary, tripleStore, new AtomicInteger());

    }

    @Test
    public void testSCM_EQC2() {
        Dictionary dictionary = new DictionaryPrimitrivesRWLock();
        TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        RunSCM_EQC2 runSCM_EQC2 = new RunSCM_EQC2(dictionary, tripleStore, new AtomicInteger());

    }

    @Test
    public void testSCM_EQP2() {
        Dictionary dictionary = new DictionaryPrimitrivesRWLock();
        TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        RunSCM_EQP2 runSCM_EQP2 = new RunSCM_EQP2(dictionary, tripleStore, new AtomicInteger());

    }

    @Test
    public void testSCM_RNG1() {
        Dictionary dictionary = new DictionaryPrimitrivesRWLock();
        TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        RunSCM_RNG1 runSCM_RNG1 = new RunSCM_RNG1(dictionary, tripleStore, new AtomicInteger());

    }

    @Test
    public void testSCM_RNG2() {
        Dictionary dictionary = new DictionaryPrimitrivesRWLock();
        TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        RunSCM_RNG2 runSCM_RNG2 = new RunSCM_RNG2(dictionary, tripleStore, new AtomicInteger());

    }

    @Test
    public void testSCM_SPO() {
        Dictionary dictionary = new DictionaryPrimitrivesRWLock();
        TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        RunSCM_SPO runSCM_SPO = new RunSCM_SPO(dictionary, tripleStore, new AtomicInteger());

    }
}
