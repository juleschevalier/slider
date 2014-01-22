package fr.ujm.tse.lt2c.satin.interfaces;

import java.util.EventListener;

/**
 * @author Jules Chevalier
 * 
 *         Interface for classes which want to be notified of buffer events
 */
public interface BufferListener extends EventListener {

    /**
     * Invocated when the buffer is full
     */
    boolean bufferFull();

}
