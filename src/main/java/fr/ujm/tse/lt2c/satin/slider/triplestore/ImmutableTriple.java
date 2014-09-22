package fr.ujm.tse.lt2c.satin.slider.triplestore;

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

import fr.ujm.tse.lt2c.satin.slider.interfaces.Triple;

/**
 * Immutable implementation of Triple
 * 
 * @author Jules Chevalier
 * @see Triple
 */
public final class ImmutableTriple implements Triple {

    private final long subject;
    private final long predicate;
    private final long object;

    /**
     * Constructor
     * 
     * @param subject
     * @param predicate
     * @param object
     */
    public ImmutableTriple(final long subject, final long predicate, final long object) {
        super();
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    @Override
    public long getSubject() {
        return this.subject;
    }

    @Override
    public long getPredicate() {
        return this.predicate;
    }

    @Override
    public long getObject() {
        return this.object;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (int) (this.object ^ (this.object >>> 32));
        result = (prime * result) + (int) (this.predicate ^ (this.predicate >>> 32));
        result = (prime * result) + (int) (this.subject ^ (this.subject >>> 32));
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
        final ImmutableTriple other = (ImmutableTriple) obj;
        if (this.object != other.object) {
            return false;
        }
        if (this.predicate != other.predicate) {
            return false;
        }
        if (this.subject != other.subject) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[" + this.subject + ", " + this.predicate + ", " + this.object + "]";
    }

}
