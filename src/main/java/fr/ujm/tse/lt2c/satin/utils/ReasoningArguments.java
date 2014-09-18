package fr.ujm.tse.lt2c.satin.utils;

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

import java.io.File;
import java.util.List;

import fr.ujm.tse.lt2c.satin.rules.ReasonerProfile;

/**
 * @author Jules Chevalier
 *
 */
public class ReasoningArguments {

    public ReasoningArguments(final int threads, final int bufferSize, final long timeout, final int iteration, final boolean cumulativeMode,
            final ReasonerProfile profile, final boolean persistMode, final boolean dumpMode, final List<File> files) {
        super();
        this.threads = threads;
        this.bufferSize = bufferSize;
        this.timeout = timeout;
        this.iteration = iteration;
        this.cumulativeMode = cumulativeMode;
        this.profile = profile;
        this.persistMode = persistMode;
        this.dumpMode = dumpMode;
        this.files = files;
    }

    /* Reasoner fields */
    private final int threads;
    private final int bufferSize;
    private final long timeout;
    private final int iteration;
    private final boolean cumulativeMode;
    private final ReasonerProfile profile;

    /* Extra fields */
    private final boolean persistMode;
    private final boolean dumpMode;
    private final List<File> files;

    public int getNbThreads() {
        return this.threads;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public long getTimeout() {
        return this.timeout;
    }

    public int getIteration() {
        return this.iteration;
    }

    public boolean isCumulativeMode() {
        return this.cumulativeMode;
    }

    public ReasonerProfile getProfile() {
        return this.profile;
    }

    public boolean isPersistMode() {
        return this.persistMode;
    }

    public boolean isDumpMode() {
        return this.dumpMode;
    }

    public List<File> getFiles() {
        return this.files;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.bufferSize;
        result = prime * result + (this.cumulativeMode ? 1231 : 1237);
        result = prime * result + (this.dumpMode ? 1231 : 1237);
        result = prime * result + (this.files == null ? 0 : this.files.hashCode());
        result = prime * result + this.iteration;
        result = prime * result + (this.persistMode ? 1231 : 1237);
        result = prime * result + (this.profile == null ? 0 : this.profile.hashCode());
        result = prime * result + this.threads;
        result = prime * result + (int) (this.timeout ^ this.timeout >>> 32);
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
        final ReasoningArguments other = (ReasoningArguments) obj;
        if (this.bufferSize != other.bufferSize) {
            return false;
        }
        if (this.cumulativeMode != other.cumulativeMode) {
            return false;
        }
        if (this.dumpMode != other.dumpMode) {
            return false;
        }
        if (this.files == null) {
            if (other.files != null) {
                return false;
            }
        } else if (!this.files.equals(other.files)) {
            return false;
        }
        if (this.iteration != other.iteration) {
            return false;
        }
        if (this.persistMode != other.persistMode) {
            return false;
        }
        if (this.profile != other.profile) {
            return false;
        }
        if (this.threads != other.threads) {
            return false;
        }
        if (this.timeout != other.timeout) {
            return false;
        }
        return true;
    }

}