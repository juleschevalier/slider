/**
 * 
 */
package fr.ujm.tse.lt2c.satin.runs.test;


/**
 * @author Jules Chevalier
 * 
 */
public class TestIndividualRuns {

    // @Test
    // public void testCAX_SCO() {
    // /*
    // * Initialization
    // */
    // final Dictionary dictionary = new DictionaryPrimitrivesRWLock();
    // final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
    //
    // final RunCAX_SCO runCAX_SCO = new RunCAX_SCO(dictionary, tripleStore, new QueuedTripleBufferLock(1000), new
    // TripleDistributor(), new AtomicInteger());
    //
    // /*
    // * Test with matching triples
    // */
    // final long c1 = dictionary.add("c1"), c2 = dictionary.add("c2"), x = dictionary.add("x");
    //
    // final Triple t1 = new ImmutableTriple(c1, AbstractDictionary.subClassOf, c2);
    // final Triple t2 = new ImmutableTriple(x, AbstractDictionary.type, c1);
    //
    // tripleStore.add(t1);
    // tripleStore.add(t2);
    //
    // runCAX_SCO.getTripleBuffer().add(t1);
    // runCAX_SCO.getTripleBuffer().add(t2);
    //
    // // Assert.assertEquals(tripleStore.size(), 2);
    //
    // runCAX_SCO.run();
    //
    // Assert.assertEquals(tripleStore.size(), 3);
    //
    // final TripleStore mustFind = new VerticalPartioningTripleStoreRWLock();
    // mustFind.add(t1);
    // mustFind.add(t2);
    // mustFind.add(new ImmutableTriple(x, AbstractDictionary.type, c2));
    //
    // Assert.assertEquals(mustFind, tripleStore);
    //
    // }
    //
    // @Test
    // public void testPRP_DOM() {
    // final Dictionary dictionary = new DictionaryPrimitrivesRWLock();
    // final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
    // final RunPRP_DOM runPRP_DOM = new RunPRP_DOM(dictionary, tripleStore, new QueuedTripleBufferLock(1000), new
    // TripleDistributor(), new AtomicInteger());
    //
    // /*
    // * Test with matching triples
    // */
    // final long x = dictionary.add("x"), y = dictionary.add("y"), p = dictionary.add("p"), c = dictionary.add("c");
    //
    // final Triple t1 = new ImmutableTriple(p, AbstractDictionary.domain, c);
    // final Triple t2 = new ImmutableTriple(x, p, y);
    //
    // tripleStore.add(t1);
    // tripleStore.add(t2);
    //
    // runPRP_DOM.getTripleBuffer().add(t1);
    // runPRP_DOM.getTripleBuffer().add(t2);
    //
    // Assert.assertEquals(tripleStore.size(), 2);
    //
    // runPRP_DOM.run();
    //
    // Assert.assertEquals(tripleStore.size(), 3);
    //
    // final TripleStore mustFind = new VerticalPartioningTripleStoreRWLock();
    // mustFind.add(t1);
    // mustFind.add(t2);
    // mustFind.add(new ImmutableTriple(x, AbstractDictionary.type, c));
    //
    // Assert.assertEquals(mustFind, tripleStore);
    //
    // }
    //
    // @Test
    // public void testPRP_RNG() {
    // final Dictionary dictionary = new DictionaryPrimitrivesRWLock();
    // final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
    // final RunPRP_RNG runPRP_RNG = new RunPRP_RNG(dictionary, tripleStore, new QueuedTripleBufferLock(1000), new
    // TripleDistributor(), new AtomicInteger());
    //
    // /*
    // * Test with matching triples
    // */
    // final long x = dictionary.add("x"), y = dictionary.add("y"), p = dictionary.add("p"), c = dictionary.add("c");
    //
    // final Triple t1 = new ImmutableTriple(p, AbstractDictionary.range, c);
    // final Triple t2 = new ImmutableTriple(x, p, y);
    //
    // tripleStore.add(t1);
    // tripleStore.add(t2);
    //
    // runPRP_RNG.getTripleBuffer().add(t1);
    // runPRP_RNG.getTripleBuffer().add(t2);
    //
    // Assert.assertEquals(tripleStore.size(), 2);
    //
    // runPRP_RNG.run();
    //
    // Assert.assertEquals(tripleStore.size(), 3);
    //
    // final TripleStore mustFind = new VerticalPartioningTripleStoreRWLock();
    // mustFind.add(t1);
    // mustFind.add(t2);
    // mustFind.add(new ImmutableTriple(y, AbstractDictionary.type, c));
    //
    // Assert.assertEquals(mustFind, tripleStore);
    //
    // }
    //
    // @Test
    // public void testSCM_SCO() {
    // final Dictionary dictionary = new DictionaryPrimitrivesRWLock();
    // final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
    // final RunSCM_SCO runSCM_SCO = new RunSCM_SCO(dictionary, tripleStore, new QueuedTripleBufferLock(1000), new
    // TripleDistributor(), new AtomicInteger());
    //
    // /*
    // * Test with matching triples
    // */
    // final long c1 = dictionary.add("c1"), c2 = dictionary.add("c2"), c3 = dictionary.add("c3");
    //
    // final Triple t1 = new ImmutableTriple(c1, AbstractDictionary.subClassOf, c2);
    // final Triple t2 = new ImmutableTriple(c2, AbstractDictionary.subClassOf, c3);
    //
    // tripleStore.add(t1);
    // tripleStore.add(t2);
    //
    // runSCM_SCO.getTripleBuffer().add(t1);
    // runSCM_SCO.getTripleBuffer().add(t2);
    //
    // Assert.assertEquals(tripleStore.size(), 2);
    //
    // runSCM_SCO.run();
    //
    // Assert.assertEquals(tripleStore.size(), 3);
    //
    // final TripleStore mustFind = new VerticalPartioningTripleStoreRWLock();
    // mustFind.add(t1);
    // mustFind.add(t2);
    // mustFind.add(new ImmutableTriple(c1, AbstractDictionary.subClassOf, c3));
    //
    // Assert.assertEquals(mustFind, tripleStore);
    //
    // }
    //
    // @Test
    // public void testPRP_SPO1() {
    // final Dictionary dictionary = new DictionaryPrimitrivesRWLock();
    // final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
    // final RunPRP_SPO1 runPRP_SPO1 = new RunPRP_SPO1(dictionary, tripleStore, new QueuedTripleBufferLock(1000), new
    // TripleDistributor(), new AtomicInteger());
    //
    // /*
    // * Test with matching triples
    // */
    // final long p1 = dictionary.add("p1"), p2 = dictionary.add("p2"), x = dictionary.add("x"), y =
    // dictionary.add("y");
    //
    // final Triple t1 = new ImmutableTriple(p1, AbstractDictionary.subPropertyOf, p2);
    // final Triple t2 = new ImmutableTriple(x, p1, y);
    //
    // tripleStore.add(t1);
    // tripleStore.add(t2);
    //
    // runPRP_SPO1.getTripleBuffer().add(t1);
    // runPRP_SPO1.getTripleBuffer().add(t2);
    //
    // Assert.assertEquals(tripleStore.size(), 2);
    //
    // runPRP_SPO1.run();
    //
    // Assert.assertEquals(tripleStore.size(), 3);
    //
    // final TripleStore mustFind = new VerticalPartioningTripleStoreRWLock();
    // mustFind.add(t1);
    // mustFind.add(t2);
    // mustFind.add(new ImmutableTriple(x, p2, y));
    //
    // Assert.assertEquals(mustFind, tripleStore);
    //
    // }
    //
    // @Test
    // public void testSCM_DOM1() {
    // final Dictionary dictionary = new DictionaryPrimitrivesRWLock();
    // final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
    // final RunSCM_DOM1 runSCM_DOM1 = new RunSCM_DOM1(dictionary, tripleStore, new QueuedTripleBufferLock(1000), new
    // TripleDistributor(), new AtomicInteger());
    //
    // /*
    // * Test with matching triples
    // */
    // final long c1 = dictionary.add("c1"), c2 = dictionary.add("c2"), p = dictionary.add("p");
    //
    // final Triple t1 = new ImmutableTriple(p, AbstractDictionary.domain, c1);
    // final Triple t2 = new ImmutableTriple(c1, AbstractDictionary.subClassOf, c2);
    //
    // tripleStore.add(t1);
    // tripleStore.add(t2);
    //
    // runSCM_DOM1.getTripleBuffer().add(t1);
    // runSCM_DOM1.getTripleBuffer().add(t2);
    //
    // Assert.assertEquals(tripleStore.size(), 2);
    //
    // runSCM_DOM1.run();
    //
    // Assert.assertEquals(tripleStore.size(), 3);
    //
    // final TripleStore mustFind = new VerticalPartioningTripleStoreRWLock();
    // mustFind.add(t1);
    // mustFind.add(t2);
    // mustFind.add(new ImmutableTriple(p, AbstractDictionary.domain, c2));
    //
    // Assert.assertEquals(mustFind, tripleStore);
    //
    // }
    //
    // @Test
    // public void testSCM_DOM2() {
    //
    // final Dictionary dictionary = new DictionaryPrimitrivesRWLock();
    // final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
    // final RunSCM_DOM2 runSCM_DOM2 = new RunSCM_DOM2(dictionary, tripleStore, new QueuedTripleBufferLock(1000), new
    // TripleDistributor(), new AtomicInteger());
    //
    // /*
    // * Test with matching triples
    // */
    // final long p1 = dictionary.add("p1"), p2 = dictionary.add("p2"), c = dictionary.add("c");
    //
    // final Triple t1 = new ImmutableTriple(p2, AbstractDictionary.domain, c);
    // final Triple t2 = new ImmutableTriple(p1, AbstractDictionary.subPropertyOf, p2);
    //
    // tripleStore.add(t1);
    // tripleStore.add(t2);
    //
    // runSCM_DOM2.getTripleBuffer().add(t1);
    // runSCM_DOM2.getTripleBuffer().add(t2);
    //
    // Assert.assertEquals(tripleStore.size(), 2);
    //
    // runSCM_DOM2.run();
    //
    // Assert.assertEquals(tripleStore.size(), 3);
    //
    // final TripleStore mustFind = new VerticalPartioningTripleStoreRWLock();
    // mustFind.add(t1);
    // mustFind.add(t2);
    // mustFind.add(new ImmutableTriple(p1, AbstractDictionary.domain, c));
    //
    // Assert.assertEquals(mustFind, tripleStore);
    //
    // }
    //
    // @Test
    // public void testSCM_EQC2() {
    //
    // final Dictionary dictionary = new DictionaryPrimitrivesRWLock();
    // final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
    // final RunSCM_EQC2 runSCM_EQC2 = new RunSCM_EQC2(dictionary, tripleStore, new QueuedTripleBufferLock(1000), new
    // TripleDistributor(), new AtomicInteger());
    //
    // /*
    // * Test with matching triples
    // */
    // final long c1 = dictionary.add("c1"), c2 = dictionary.add("c2");
    //
    // final Triple t1 = new ImmutableTriple(c1, AbstractDictionary.subClassOf, c2);
    // final Triple t2 = new ImmutableTriple(c2, AbstractDictionary.subClassOf, c1);
    //
    // tripleStore.add(t1);
    // tripleStore.add(t2);
    //
    // runSCM_EQC2.getTripleBuffer().add(t1);
    // runSCM_EQC2.getTripleBuffer().add(t2);
    //
    // Assert.assertEquals(tripleStore.size(), 2);
    // runSCM_EQC2.run();
    //
    // Assert.assertEquals(tripleStore.size(), 4);
    //
    // final TripleStore mustFind = new VerticalPartioningTripleStoreRWLock();
    // mustFind.add(t1);
    // mustFind.add(t2);
    // mustFind.add(new ImmutableTriple(c1, AbstractDictionary.equivalentClass, c2));
    // mustFind.add(new ImmutableTriple(c2, AbstractDictionary.equivalentClass, c1));
    //
    // Assert.assertEquals(mustFind, tripleStore);
    //
    // }
    //
    // @Test
    // public void testSCM_EQP2() {
    //
    // final Dictionary dictionary = new DictionaryPrimitrivesRWLock();
    // final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
    // final RunSCM_EQP2 runSCM_EQC2 = new RunSCM_EQP2(dictionary, tripleStore, new QueuedTripleBufferLock(1000), new
    // TripleDistributor(), new AtomicInteger());
    //
    // /*
    // * Test with matching triples
    // */
    // final long p1 = dictionary.add("p1"), p2 = dictionary.add("p2");
    //
    // final Triple t1 = new ImmutableTriple(p1, AbstractDictionary.subPropertyOf, p2);
    // final Triple t2 = new ImmutableTriple(p2, AbstractDictionary.subPropertyOf, p1);
    //
    // tripleStore.add(t1);
    // tripleStore.add(t2);
    //
    // runSCM_EQC2.getTripleBuffer().add(t1);
    // runSCM_EQC2.getTripleBuffer().add(t2);
    //
    // Assert.assertEquals(tripleStore.size(), 2);
    // runSCM_EQC2.run();
    //
    // Assert.assertEquals(tripleStore.size(), 4);
    //
    // final TripleStore mustFind = new VerticalPartioningTripleStoreRWLock();
    // mustFind.add(t1);
    // mustFind.add(t2);
    // mustFind.add(new ImmutableTriple(p1, AbstractDictionary.equivalentProperty, p2));
    // mustFind.add(new ImmutableTriple(p2, AbstractDictionary.equivalentProperty, p1));
    //
    // Assert.assertEquals(mustFind, tripleStore);
    //
    // }
    //
    // @Test
    // public void testSCM_RNG1() {
    //
    // final Dictionary dictionary = new DictionaryPrimitrivesRWLock();
    // final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
    // final RunSCM_RNG1 runSCM_RNG1 = new RunSCM_RNG1(dictionary, tripleStore, new QueuedTripleBufferLock(1000), new
    // TripleDistributor(), new AtomicInteger());
    //
    // /*
    // * Test with matching triples
    // */
    // final long p = dictionary.add("p"), c1 = dictionary.add("c1"), c2 = dictionary.add("c2");
    //
    // final Triple t1 = new ImmutableTriple(p, AbstractDictionary.range, c1);
    // final Triple t2 = new ImmutableTriple(c1, AbstractDictionary.subClassOf, c2);
    //
    // tripleStore.add(t1);
    // tripleStore.add(t2);
    //
    // runSCM_RNG1.getTripleBuffer().add(t1);
    // runSCM_RNG1.getTripleBuffer().add(t2);
    //
    // Assert.assertEquals(tripleStore.size(), 2);
    // runSCM_RNG1.run();
    //
    // Assert.assertEquals(tripleStore.size(), 3);
    //
    // final TripleStore mustFind = new VerticalPartioningTripleStoreRWLock();
    // mustFind.add(t1);
    // mustFind.add(t2);
    // mustFind.add(new ImmutableTriple(p, AbstractDictionary.range, c2));
    //
    // Assert.assertEquals(mustFind, tripleStore);
    //
    // }
    //
    // @Test
    // public void testSCM_RNG2() {
    //
    // final Dictionary dictionary = new DictionaryPrimitrivesRWLock();
    // final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
    // final RunSCM_RNG2 runSCM_RNG2 = new RunSCM_RNG2(dictionary, tripleStore, new QueuedTripleBufferLock(1000), new
    // TripleDistributor(), new AtomicInteger());
    //
    // /*
    // * Test with matching triples
    // */
    // final long p1 = dictionary.add("p1"), p2 = dictionary.add("p2"), c = dictionary.add("c");
    //
    // final Triple t1 = new ImmutableTriple(p2, AbstractDictionary.range, c);
    // final Triple t2 = new ImmutableTriple(p1, AbstractDictionary.subPropertyOf, p2);
    //
    // tripleStore.add(t1);
    // tripleStore.add(t2);
    //
    // runSCM_RNG2.getTripleBuffer().add(t1);
    // runSCM_RNG2.getTripleBuffer().add(t2);
    //
    // Assert.assertEquals(tripleStore.size(), 2);
    // runSCM_RNG2.run();
    //
    // Assert.assertEquals(tripleStore.size(), 3);
    //
    // final TripleStore mustFind = new VerticalPartioningTripleStoreRWLock();
    // mustFind.add(t1);
    // mustFind.add(t2);
    // mustFind.add(new ImmutableTriple(p1, AbstractDictionary.range, c));
    //
    // Assert.assertEquals(mustFind, tripleStore);
    //
    // }
    //
    // @Test
    // public void testSCM_SPO() {
    //
    // final Dictionary dictionary = new DictionaryPrimitrivesRWLock();
    // final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
    // final RunSCM_SPO runSCM_SPO = new RunSCM_SPO(dictionary, tripleStore, new QueuedTripleBufferLock(1000), new
    // TripleDistributor(), new AtomicInteger());
    //
    // /*
    // * Test with matching triples
    // */
    // final long p1 = dictionary.add("p1"), p2 = dictionary.add("p2"), p3 = dictionary.add("p3");
    //
    // final Triple t1 = new ImmutableTriple(p1, AbstractDictionary.subPropertyOf, p2);
    // final Triple t2 = new ImmutableTriple(p2, AbstractDictionary.subPropertyOf, p3);
    //
    // tripleStore.add(t1);
    // tripleStore.add(t2);
    //
    // runSCM_SPO.getTripleBuffer().add(t1);
    // runSCM_SPO.getTripleBuffer().add(t2);
    //
    // Assert.assertEquals(tripleStore.size(), 2);
    // runSCM_SPO.run();
    //
    // Assert.assertEquals(tripleStore.size(), 3);
    //
    // final TripleStore mustFind = new VerticalPartioningTripleStoreRWLock();
    // mustFind.add(t1);
    // mustFind.add(t2);
    // mustFind.add(new ImmutableTriple(p1, AbstractDictionary.subPropertyOf, p3));
    //
    // Assert.assertEquals(mustFind, tripleStore);
    //
    // }
}
