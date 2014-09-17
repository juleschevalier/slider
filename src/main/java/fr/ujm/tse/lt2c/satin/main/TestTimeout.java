package fr.ujm.tse.lt2c.satin.main;

import java.util.Collection;
import java.util.HashSet;

import fr.ujm.tse.lt2c.satin.dictionary.AbstractDictionary;
import fr.ujm.tse.lt2c.satin.dictionary.DictionaryPrimitrivesRWLock;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.reasoner.ReasonerStreamed;
import fr.ujm.tse.lt2c.satin.rules.ReasonerProfile;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

public class TestTimeout {

    private TestTimeout() {
    }

    public static void main(final String[] args) {

        final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        final Dictionary dictionary = new DictionaryPrimitrivesRWLock();
        final ReasonerStreamed reasoner = new ReasonerStreamed(tripleStore, dictionary, ReasonerProfile.RHODF, 1000);

        reasoner.start();
        /****************/

        final Collection<Triple> triples = new HashSet<>();

        /* Add subproperties */
        long a = 1, b = -1;
        for (int i = 0; i < 500; i++) {
            triples.add(new ImmutableTriple(a++, AbstractDictionary.subPropertyOf, b--));
        }
        tripleStore.addAll(triples);
        reasoner.addTriples(triples);
        triples.clear();

        /* Add subclasses */
        for (int j = 0; j < 1000; j++) {
            for (int i = 0; i < 1000; i++) {
                triples.add(new ImmutableTriple(a++, AbstractDictionary.subClassOf, b--));
            }
            tripleStore.addAll(triples);
            reasoner.addTriples(triples);
            triples.clear();
        }

        /* Add subproperties */
        for (int i = 0; i < 500; i++) {
            triples.add(new ImmutableTriple(a++, AbstractDictionary.subPropertyOf, b--));
        }
        tripleStore.addAll(triples);
        reasoner.addTriples(triples);
        triples.clear();

        /* Add subclasses */
        for (int j = 0; j < 1000; j++) {
            for (int i = 0; i < 1000; i++) {
                triples.add(new ImmutableTriple(a++, AbstractDictionary.subClassOf, b--));
            }
            tripleStore.addAll(triples);
            reasoner.addTriples(triples);
            triples.clear();
        }

        /****************/
        reasoner.close();

        try {
            reasoner.join();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Total: " + tripleStore.size());
        System.out.println("SCO: " + tripleStore.getbyPredicate(AbstractDictionary.subClassOf).size());
        System.out.println("SPO: " + tripleStore.getbyPredicate(AbstractDictionary.subPropertyOf).size());
    }
}
