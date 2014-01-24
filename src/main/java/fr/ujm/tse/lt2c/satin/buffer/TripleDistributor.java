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
    public void addSubscriber(TripleBuffer tripleBuffer, long[] predicates) {
        if (predicates.length == 0) {
            if (!this.universalSubscribers.contains(tripleBuffer)) {
                this.universalSubscribers.add(tripleBuffer);
            }
            return;
        }
        for (Long predicate : predicates) {
            if (!this.subscribers.containsEntry(predicate, tripleBuffer)) {
                this.subscribers.put(predicate, tripleBuffer);
            }
        }
    }

    @Override
    public void run() {

        if (logger.isTraceEnabled()) {
            logger.trace(debugName + " Distributor run");
        }

        while (running) {
            try {
                Triple triple = this.tripleQueue.poll(1, TimeUnit.DAYS);
                for (TripleBuffer tripleBuffer : this.subscribers.get(triple.getPredicate())) {
                    synchronized (tripleBuffer) {

                        while (!tripleBuffer.add(triple)) {
                            if (logger.isTraceEnabled()) {
                                logger.trace("TD buffer add failed");
                            }
                            try {
                                tripleBuffer.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                for (TripleBuffer tripleBuffer : this.universalSubscribers) {
                    synchronized (tripleBuffer) {

                        while (!tripleBuffer.add(triple)) {
                            if (logger.isTraceEnabled()) {
                                logger.trace("TD buffer add failed");
                            }
                            try {
                                tripleBuffer.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
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
    public void distribute(Collection<Triple> triples) {
        /*
         * ISSUE -> ALL BUFFERS WAIT FOR ONE BLOCKED
         */
        long debugDistributed = 0;
        for (Triple triple : triples) {
            if (logger.isTraceEnabled()) {
                logger.trace("TD send " + triple);
            }
            long p = triple.getPredicate();
            for (TripleBuffer tripleBuffer : this.subscribers.get(p)) {
                synchronized (tripleBuffer) {

                    while (!tripleBuffer.add(triple)) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("TD buffer add failed");
                        }
                        try {
                            tripleBuffer.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                debugDistributed++;
            }
            for (TripleBuffer tripleBuffer : this.universalSubscribers) {
                synchronized (tripleBuffer) {

                    while (!tripleBuffer.add(triple)) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("TD buffer add failed");
                        }
                        try {
                            tripleBuffer.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                debugDistributed++;
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace(debugName + " " + debugDistributed + " triples sent (" + triples.size() + " unique triples, " + (this.subscribers.size() + this.universalSubscribers.size()) + " subscribers)");
        }
    }

    /**
     * @param name
     */
    public void setName(String name) {
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
    public String subscribers(String name, Dictionary dictionary) {
        StringBuilder subs = new StringBuilder();
        subs.append("\n");
        for (TripleBuffer buffer : universalSubscribers) {
            subs.append(name + " send to " + buffer.getDebugName() + " for *\n");
        }
        for (Long predicate : subscribers.keySet()) {
            for (TripleBuffer buffer : subscribers.get(predicate)) {
                subs.append(name + " send to " + buffer.getDebugName() + " for " + dictionary.printConcept(dictionary.get(predicate)) + "\n");
            }
        }
        return subs.toString();
    }

    public BlockingQueue<Triple> getTripleQueue() {
        return tripleQueue;
    }

}
