#SLIDER

##What is Slider?

Slider is a forward-chaining reasoner supporting the following rule sets:

 - RhoDF Default
 - RhoDF Full
 - RDFS Default
 - RDFS Full

Slider allows to custom these fragments by selecting the rules to use for the inference.
Additional rules can be easily added by implementing a single method.

Slider provides both batch and stream reasoning.

It is a full Java project.

##Contributors

 - Jules Chevalier (jules.chevalier@univ-st-etienne.fr)
 - Julien Subercaze (julien.subercaze@univ-st-etienne.fr)
 - Christophe Gravier (christophe.gravier@univ-st-etienne.fr)
 - Frédérique Laforest (frederique.laforest@univ-st-etienne.fr)

##Requirements

 - **Java 1.7** or greater
 - **Git** to clone the project
 - **Maven** to install and run it easily

<!--
###Necessary Libraries

The following libraries are needed to compile and run Slider.
They are included as Maven dependencies in the pom.xml file.

 - apache-jena-libs
 - junit
 - log4j
 - guava
 - morphia
 - mongo-java-driver
 - commons-cli
-->

##Installation

To install Slider, clone the repository and compile it through Maven:

```bash
git clone git@github.com:juleschevalier/slider.git
cd slider/
mvn install
```

## Running Slider

###Command Line use

Slider can be run as a standalone software.
The exec-maven-plugin is configured, and can be used to run it.
For example, for launch the inference on the file "toto.nt" for RDFS:
```bash
mvn exec:java -q -Dexec.args="-p RDFS toto.nt"
```

Here is the list of the different options (accesible via -h,--help option):
```
 -b,--buffer-size <time>......set the buffer size
 -d,--directory <directory>.. infers on all ontologies in the directory
 -h,--help....................print this message
 -i,--iteration <number>......how many times each file
 -n,--threads <number>........set the number of threads by available core (0 means the jvm manage)
 -o,--output..................save output into file
 -p,--profile <profile>...... set the fragment [RHODF, BRHODF, RDFS, BRDFS]
 -r,--batch-reasoning........ enable batch reasoning
 -t,--timeout <arg>.......... set the buffer timeout in ms (0 means timeout will be disabled)
 -v,--verbose................ enable verbose mode
 -w,--warm-up................ insert a warm-up lap before the inference
```

####Examples
Reason on all ontologies in the folder "~/Ontologies" with RDFS
```bash
mvn exec:java -q -Dexec.args="-p RDFS -d ~/Ontologies"
```
Reason over the ontology "ontology1.nt" with RhoDF with a buffer timeout of 1000ms
```bash
mvn exec:java -q -Dexec.args="-p RhoDF -t 1000 ~/Ontologies/ontology1.nt"
```
Reason on all ontologies in the folder "~/Ontologies" with RDFS with a warm-up lap (no included in results) and 10 iterations for each file
```bash
mvn exec:java -q -Dexec.args="-p RDFS -w -i 10 -d ~/Ontologies"
```
Reason over the ontology "ontology1.nt" with RhoDF and write the output into "infered1.nt"
```bash
mvn exec:java -q -Dexec.args="-p RhoDF -o infered.nt ~/Ontologies/ontology1.nt"
```

###Use Slider's API

Slider can also be used as a library.
It provides both bash and stream reasoning.

####Batch reasoning
```Java
final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
final Dictionary dictionary = new DictionaryPrimitrivesRWLock();
final ReasonerStreamed reasoner = new ReasonerStreamed(tripleStore,
													   dictionary,
													   ReasonerProfile.RDFS);

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
```

####Stream reasoning
```Java
final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
final Dictionary dictionary = new DictionaryPrimitrivesRWLock();
final ReasonerStreamed reasoner = new ReasonerStreamed(tripleStore,
													   dictionary,
													   ReasonerProfile.RDFS);

final Parser parser = new ParserImplNaive(dictionary, tripleStore);
reasoner.start();

parser.parseStream(file.getAbsolutePath(), reasoner);

reasoner.close();
try {
    reasoner.join();
} catch (final InterruptedException e) {
    e.printStackTrace();
}
```

##Maven dependency

Setup the server in your pom.xml:

```xml
<repositories>
    <repository>
        <id>slider-mvn-repo</id>
        <url>https://raw.github.com/juleschevalier/slider/mvn-repo/</url>
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
    </repository>
</repositories>
```

Then use the following dependency :

```xml
<dependency>
    <groupId>fr.ujm.tse.lt2c.satin</groupId>
    <artifactId>slider</artifactId>
    <version>0.9.5-SNAPSHOT</version>
</dependency>
```
Have a look [here](https://github.com/juleschevalier/slider/tree/mvn-repo/fr/ujm/tse/lt2c/satin/slider) to see all the available versions

##Correctness

The correctness of Slider's inference has been verified using [Jena](https://jena.apache.org/documentation/inference/index.html)'s reasoner as ground truth.

##How to reproduce

All the details to reproduce our experimentations on Slider.

###Ontologies

The [BSBM](http://wifo5-03.informatik.uni-mannheim.de/bizer/berlinsparqlbenchmark/) ontologies have been generated thanks to this [script](https://gist.github.com/cgravier/8658389).
It generates the following ontologies:
 * dataset_100k.nt
 * dataset_200k.nt
 * dataset_500k.nt
 * dataset_1M.nt
 * dataset_5M.nt
 * dataset_10M.nt
 * dataset_25M.nt
 * dataset_50M.nt
 * dataset_100M.nt

The subClassOf ontologies have been generated thanks to this [script](https://gist.github.com/JulesChevalier/4bd3410cf14bd51e9811).
We used it to generate the following ontologies:
 * subClassOf10.nt
 * subClassOf20.nt
 * subClassOf50.nt
 * subClassOf100.nt
 * subClassOf200.nt
 * subClassOf500.nt

You can download the two real life zipped ontologies here:
 * [wikipediaOntology.zip](http://datasets-satin.telecom-st-etienne.fr/cgravier/inferray/wikipediaOntology.zip)
 * [wordnetOntology.zip](http://datasets-satin.telecom-st-etienne.fr/cgravier/inferray/wordnetOntology.zip)

All these ontologies can be downloaded [here](http://datasets-satin.telecom-st-etienne.fr/jchevalier/slider/benchmark)

The other ontologies used for anterior experimentations can be found [here](http://datasets-satin.telecom-st-etienne.fr/jchevalier/slider/tuning/)

###Run the experimentations

To benchmark Slider's performances, the easiest way is to use the commande line interface provided by the Main class.
You can use the maven exec plugin preconfigured.
First install Slider (see section Installation).
For better results, we launch the inference 10 times on each ontology, and used the average time.
We also add a *warm-up* lap, which is a first iteration of inference which does not appear in the results.
This first step allow Java to operate the on-the-fly optimisation of the binaries.

To run an experimentation on the ontologies contained in a folder "Ontologies" for RDFS, please use the following command
```bash
mvn exec:java -q -Dexec.args="-p RDFS -i 10 -w -d ~/Ontologies"
```

To run experimentation on the parameters of Slider, please use the same command with the corresponding options:
```bash
 -b,--buffer-size <time>......set the buffer size
 -n,--threads <number>........set the number of threads by available core (0 means the jvm manage)
 -t,--timeout <arg>.......... set the buffer timeout in ms (0 means timeout will be disabled)
```

##Bug reports

If you discover any bug, feel free to create an issue on GitHub: https://github.com/juleschevalier/slider/issues.

##Licence

Slider is provided under Apache License, Version 2.0.
You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

##Contact

For any question, please contact us at jules.chevalier@univ-st-etienne.fr

##Acknowledgement

This work was funded by the French Fonds national pour la Société Numérique(FSN), and supported by Pôles Minalogic, Systematic and SCS.
It is part of the [Open Clouware](http://www.opencloudware.org/bin/view/Main/) project.