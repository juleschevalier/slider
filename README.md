#SLIDER

## What is Slider?

Slider is a forward-chaining reasoner supporting the following rule sets:

 - RhoDF
 - RDFS

Slider allows to custom these fragments by selecting the rules to use for the inference.
Additional rules can be easily added by implementing a single method.

Slider provides both batch and stream reasoning.

## Requirements

### Necessary Libraries

The following libraries are needed to compile and run Slider.
They are included as Maven dependencies in the pom.xml file.

 - apache-jena-libs
 - junit
 - log4j
 - guava
 - morphia
 - mongo-java-driver
 - commons-cli

## Installation

To install Slider, clone the repository and compile Slider through Maven:

```bash
git clone git@github.com:telecom-se/distributed-reasoner.git
cd distributed-reasoner/
mvn clean compile install
```

## Running Slider

Slider can be run as a standalone software.
The exec-maven-plugin is configured, and can be used to run it.
For example, for launch the inference on the file "toto.nt" for RDFS:
```bash
mvn exec:java -q -Dexec.args="-p RDFS toto.nt"
```

Here is the list of the different options:
```
usage: main
 -b,--buffer-size <time>      set the buffer size
 -c,--cumulative              does not reinit data for each file
 -d,--directory <directory>   infers on all ontologies in the directory
 -h,--help                    print this message
 -i,--iteration <number>      how many times each file
 -m,--mongo-save              persists the results in MongoDB
 -n,--threads <number>        set the number of threads by available core (0 means the jvm manage)
 -o,--output                  save output into file
 -p,--profile <profile>       set the fragment [RHODF, BRHODF, RDFS, BRDFS]
 -t,--timeout <arg>           set the buffer timeout in ms (0 means no timeout)
```
## Code examples

Slider can also be used as an external library.
It provides both bash and stream reasoning.

### Batch reasoning
        /* */
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

### Stream reasoning

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

## Maven dependency

## How to reproduce

## Correctness

## Licence

Slider is provided under Apache License, Version 2.0
You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

## Contact

For any question, please contact us at: jules.chevalier@univ-st-etienne.fr