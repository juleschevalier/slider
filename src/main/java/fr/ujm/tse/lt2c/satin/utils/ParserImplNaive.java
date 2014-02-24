package fr.ujm.tse.lt2c.satin.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Triple;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.ImmutableTriple;

public class ParserImplNaive implements Parser {

    private static Logger logger = Logger.getLogger(ParserImplNaive.class);

    Dictionary dictionary;
    TripleStore tripleStore;

    /**
     * @param f
     *            the file to parse
     */
    public ParserImplNaive(final Dictionary dictionary, final TripleStore tripleStore) {
        this.dictionary = dictionary;
        this.tripleStore = tripleStore;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ujm.tse.lt2c.satin.Parser#parse()
     */
    @Override
    public void parse(final String fileInput) {

        final PipedRDFIterator<Triple> iter = new PipedRDFIterator<Triple>();
        final PipedRDFStream<Triple> inputStream = new PipedTriplesStream(iter);

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Runnable parser = new Runnable() {

            @Override
            public void run() {
                // Call the parsing process.
                RDFDataMgr.parse(inputStream, fileInput);
            }
        };

        final long debugStartTime = System.nanoTime();
        executor.submit(parser);
        while (iter.hasNext()) {
            final Triple next = iter.next();
            this.addTriple(next);
        }
        final long debugEndTime = System.nanoTime();

        if (logger.isInfoEnabled()) {
            logger.info("Parsing : " + this.tripleStore.size() + " triples in " + ((debugEndTime - debugStartTime) / 1000000) + "ms");
        }

        if (logger.isTraceEnabled()) {
            logger.trace("---DICTIONARY---");
            logger.trace(this.dictionary.printDico());
            logger.trace("----TRIPLES-----");
            for (final fr.ujm.tse.lt2c.satin.interfaces.Triple triple : this.tripleStore.getAll()) {
                if (logger.isTraceEnabled()) {
                    logger.trace(triple + " " + this.dictionary.printTriple(triple));
                }
            }
            logger.trace("-------------");
        }
    }

    private void addTriple(final Triple next) {
        final String s = next.getSubject().toString();
        final String p = next.getPredicate().toString();
        final String o = next.getObject().toString();

        final long si = this.dictionary.add(s);
        final long pi = this.dictionary.add(p);
        final long oi = this.dictionary.add(o);

        this.tripleStore.add(new ImmutableTriple(si, pi, oi));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((this.dictionary == null) ? 0 : this.dictionary.hashCode());
        result = (prime * result) + ((this.tripleStore == null) ? 0 : this.tripleStore.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final ParserImplNaive other = (ParserImplNaive) obj;
        if (this.dictionary == null) {
            if (other.dictionary != null) {
                return false;
            }
        } else if (!this.dictionary.equals(other.dictionary)) {
            return false;
        }
        if (this.tripleStore == null) {
            if (other.tripleStore != null) {
                return false;
            }
        } else if (!this.tripleStore.equals(other.tripleStore)) {
            return false;
        }
        return true;
    }

}
