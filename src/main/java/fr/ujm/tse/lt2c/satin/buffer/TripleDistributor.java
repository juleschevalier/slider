package fr.ujm.tse.lt2c.satin.buffer;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.BlockingArrayQueue;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;

public class TripleDistributor implements Runnable {

    private static Logger logger = Logger.getLogger(TripleDistributor.class);

    private final Multimap<Long, TripleBuffer> subscribers;
    private final Collection<TripleBuffer> universalSubscribers;
    private final BlockingQueue<Triple> tripleQueue;
    private String debugName = "";
    private final boolean running = true;

    /**
     * Constructor
     */
    public TripleDistributor() {
        super();
        this.tripleQueue = new BlockingArrayQueue<>();

        this.subscribers = HashMultimap.create();
        this.universalSubscribers = new HashSet<>();
    }

    /**
     * Add a TripleBuffer as subscribers for predicates
     * 
     * @param tripleBuffer
     * @param predicates
     * @see TripleBuffer
     */
    public void addSubscriber(final TripleBuffer tripleBuffer, final long[] predicates) {
        if (predicates.length == 0) {
            if (!this.universalSubscribers.contains(tripleBuffer)) {
                this.universalSubscribers.add(tripleBuffer);
            }
            return;
        }
        for (final Long predicate : predicates) {
            if (!this.subscribers.containsEntry(predicate, tripleBuffer)) {
                this.subscribers.put(predicate, tripleBuffer);
            }
        }
    }

    @Override
    public void run() {

        if (logger.isTraceEnabled()) {
            logger.trace(this.debugName + " Distributor run");
        }

        while (this.running) {
            try {
                final Triple triple = this.tripleQueue.poll(1, TimeUnit.DAYS);
                for (final TripleBuffer tripleBuffer : this.subscribers.get(triple.getPredicate())) {
                    synchronized (tripleBuffer) {

                        while (!tripleBuffer.add(triple)) {
                            if (logger.isTraceEnabled()) {
                                logger.trace("TD buffer add failed");
                            }
                            try {
                                tripleBuffer.wait();
                            } catch (final InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                for (final TripleBuffer tripleBuffer : this.universalSubscribers) {
                    synchronized (tripleBuffer) {

                        while (!tripleBuffer.add(triple)) {
                            if (logger.isTraceEnabled()) {
                                logger.trace("TD buffer add failed");
                            }
                            try {
                                tripleBuffer.wait();
                            } catch (final InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Send all the triples to all matching subscribers
     * 
     * @param triples
     * @see Triple
     */
    public long distributeAll(final TripleStore triples) {
        long debugDistributed = 0;
        Collection<Triple> toDistribute;
        for (final long predicate : triples.getPredicates()) {
            toDistribute = triples.getbyPredicate(predicate);
            for (final TripleBuffer tripleBuffer : this.subscribers.get(predicate)) {
                tripleBuffer.addAll(toDistribute);
                debugDistributed++;
            }
            for (final TripleBuffer tripleBuffer : this.universalSubscribers) {
                tripleBuffer.addAll(toDistribute);
                debugDistributed++;
            }
        }
        return debugDistributed;
    }

    /**
     * Send all the triples to all matching subscribers
     * 
     * @param triples
     * @see Triple
     */
    public void distributeAll(final Collection<Triple> triples) {
        /*
         * ISSUE -> ALL BUFFERS WAIT FOR ONE BLOCKED
         * => NO MORE WITH QUEUES
         */
        long debugDistributed = 0;
        for (final Triple triple : triples) {
            debugDistributed += this.ditribute(triple);
        }
        if (logger.isTraceEnabled()) {
            logger.trace(this.debugName + " " + debugDistributed + " triples sent (" + triples.size() + " unique triples, "
                    + (this.subscribers.size() + this.universalSubscribers.size()) + " subscribers)");
        }
    }

    public long ditribute(final Triple triple) {
        long debugDistributed = 0;
        if (logger.isTraceEnabled()) {
            logger.trace("TD send " + triple);
        }
        final long p = triple.getPredicate();
        for (final TripleBuffer tripleBuffer : this.subscribers.get(p)) {
            tripleBuffer.add(triple);
            debugDistributed++;
        }
        for (final TripleBuffer tripleBuffer : this.universalSubscribers) {
            tripleBuffer.add(triple);
            debugDistributed++;
        }
        return debugDistributed;
    }

    /**
     * @param name
     */
    public void setName(final String name) {
        this.debugName = name;
    }

    /**
     * @return the number of subscribers
     */
    public int subscribersNumber() {
        return this.subscribers.values().size() + this.universalSubscribers.size();
    }

    /**
     * @param name
     * @param dictionary
     * @return A string listing all subscribers with their predicates. Only for
     *         debugging
     * @see Dictionary
     */
    public String subscribers(final String name, final Dictionary dictionary) {
        final StringBuilder subs = new StringBuilder();
        subs.append("\n");
        for (final TripleBuffer buffer : this.universalSubscribers) {
            subs.append(name + " send to " + buffer.getDebugName() + " for *\n");
        }
        for (final Long predicate : this.subscribers.keySet()) {
            for (final TripleBuffer buffer : this.subscribers.get(predicate)) {
                subs.append(name + " send to " + buffer.getDebugName() + " for " + dictionary.printConcept(dictionary.get(predicate)) + "\n");
            }
        }
        return subs.toString();
    }

    public BlockingQueue<Triple> getTripleQueue() {
        return this.tripleQueue;
    }

}
