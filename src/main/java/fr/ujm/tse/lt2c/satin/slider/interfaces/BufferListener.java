package fr.ujm.tse.lt2c.satin.slider.interfaces;

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
     * 
     * @return true if the buffer is full, false otherwise
     */
    boolean bufferFull();

}
