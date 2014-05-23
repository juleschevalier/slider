package fr.ujm.tse.lt2c.satin.interfaces;

import java.util.EventListener;

/**
 * @author Jules Chevalier
 * 
 *         Interface for classes which want to be notified of buffer events
 * @see TripleBuffer
 */
public interface BufferListener extends EventListener {

    /**
     * Method invocated when the buffer is full
     */
    boolean bufferFull();

}
