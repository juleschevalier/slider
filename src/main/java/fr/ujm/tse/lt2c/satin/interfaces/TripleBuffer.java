package fr.ujm.tse.lt2c.satin.interfaces;

/*
 * #%L
 * SLIDeR
 * %%
 * Copyright (C) 2014 Université Jean Monnet, Saint Etienne
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Collection;

/**
 * Interface for FIFO triple buffer
 * 
 * @author Jules Chevalier
 * 
 */
public interface TripleBuffer {

    /**
     * Add a triple to the buffer.
     * Notifies subcsribers of "bufferfull" if needed
     * 
     * @param triple
     * @see Triple
     */
    void add(Triple triple);

    /**
     * @return the bufferSize first triples inserted in a TripleStore
     * @see TripleStore
     */
    TripleStore clear();

    /**
     * Add a listener to the buffer's events
     * 
     * @param bufferListener
     * @see BufferListener
     */
    void addBufferListener(BufferListener bufferListener);

    /**
     * @return the buffer listeners in a collection
     * @see BufferListener
     */
    Collection<BufferListener> getBufferListeners();

    /**
     * @return the limit size when bufferFull() is sent
     */
    long getBufferLimit();

    /**
     * @return all triples in the main buffer
     * @see Triple
     */
    Collection<Triple> getCollection();

    /**
     * Notifies all subscribers that the buffer is full
     */
    void sendFullBuffer();

    /**
     * For debugging, set the name used by logger
     * 
     * @param name
     */
    void setDebugName(String name);

    /**
     * @return the name used by logger for debugging
     */
    String getDebugName();

    /**
     * @return the number of triples stored
     */
    long getOccupation();

    /**
     * Add all the triples to the buffer.
     * Notifies subcsribers of "bufferfull" if needed
     * 
     * @param triples
     * @see Triple
     */
    void addAll(Collection<Triple> triples);

}
