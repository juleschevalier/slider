#Slider

Slider is a forward-chaining reasoner supporting the following rule sets:

 - RhoDF
 - RDFS

Slider allows to custom these fragments by selecting the rules to use for the inference.
Additionnal rules can be easily added by implementing a single method.

Slider provides both batch and stream reasoning.

##Code examples

###Batch reasoning

        final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        final Dictionary dictionary = new DictionaryPrimitrivesRWLock();
        final ReasonerStreamed reasoner = new ReasonerStreamed(tripleStore, dictionary, arguments.getProfile(), arguments.getTimeout());

        final Parser parser = new ParserImplNaive(dictionary, tripleStore);
        final Collection<Triple> triples = parser.parse(file.getAbsolutePath());
        reasoner.start();

        reasoner.addTriples(triples);

        reasoner.close();
        try {
            reasoner.join();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

###Stream reasoning

		final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        final Dictionary dictionary = new DictionaryPrimitrivesRWLock();
        final ReasonerStreamed reasoner = new ReasonerStreamed(tripleStore, dictionary, arguments.getProfile(), arguments.getTimeout());

        final long start = System.nanoTime();
        reasoner.start();

        final Parser parser = new ParserImplNaive(dictionary, tripleStore);
        parser.parseStream(file.getAbsolutePath(), reasoner);

        reasoner.close();
        try {
            reasoner.join();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

##Maven dependency

##How to reproduce