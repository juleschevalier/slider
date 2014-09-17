package fr.ujm.tse.lt2c.satin.main;

import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

public class TestRemove {

    public static void main(final String[] args) {

        final TripleStore ts = new VerticalPartioningTripleStoreRWLock();

        final Triple t1 = new ImmutableTriple(0, 1, 2);
        final Triple t2 = new ImmutableTriple(4, 5, 6);
        final Triple t3 = new ImmutableTriple(7, 8, 9);

        System.out.println("add " + t1);
        ts.add(t1);
        System.out.println("ts " + ts.size() + " " + ts.getAll());
        System.out.println("add " + t2);
        ts.add(t2);
        System.out.println("ts " + ts.size() + " " + ts.getAll());
        System.out.println("remove " + t1);
        ts.remove(t1);
        System.out.println("ts " + ts.size() + " " + ts.getAll());
        System.out.println("remove " + t3);
        ts.remove(t3);
        System.out.println("ts " + ts.size() + " " + ts.getAll());

    }

}
