package fr.ujm.tse.lt2c.satin.slider.utils;

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

import fr.ujm.tse.lt2c.satin.slider.rules.ReasonerProfile;

/**
 * @author Jules Chevalier
 *
 */
public class ReasoningArguments {

    public ReasoningArguments(final int threads, final int bufferSize, final long timeout, final int iteration, final ReasonerProfile profile,
            final boolean verboseMode, final boolean warmupMode, final boolean dumpMode, final boolean batchMode, final List<File> files) {
        super();
        this.threadsNb = threads;
        this.bufferSize = bufferSize;
        this.timeout = timeout;
        this.iteration = iteration;
        this.profile = profile;
        this.verboseMode = verboseMode;
        this.warmupMode = warmupMode;
        this.dumpMode = dumpMode;
        this.batchMode = batchMode;
        this.files = files;
    }

    /* Reasoner fields */
    private final int threadsNb;
    private final int bufferSize;
    private final long timeout;
    private final int iteration;
    private final ReasonerProfile profile;

    /* Extra fields */
    private final boolean verboseMode;
    private final boolean warmupMode;
    private final boolean dumpMode;
    private final boolean batchMode;
    private final List<File> files;

    public int getThreadsNb() {
        return this.threadsNb;
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

    public ReasonerProfile getProfile() {
        return this.profile;
    }

    public boolean isVerboseMode() {
        return this.verboseMode;
    }

    public boolean isWarmupMode() {
        return this.warmupMode;
    }

    public boolean isDumpMode() {
        return this.dumpMode;
    }

    public boolean isBatchMode() {
        return this.batchMode;
    }

    public List<File> getFiles() {
        return this.files;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.batchMode ? 1231 : 1237);
        result = prime * result + this.bufferSize;
        result = prime * result + (this.dumpMode ? 1231 : 1237);
        result = prime * result + (this.files == null ? 0 : this.files.hashCode());
        result = prime * result + this.iteration;
        result = prime * result + (this.profile == null ? 0 : this.profile.hashCode());
        result = prime * result + this.threadsNb;
        result = prime * result + (int) (this.timeout ^ this.timeout >>> 32);
        result = prime * result + (this.verboseMode ? 1231 : 1237);
        result = prime * result + (this.warmupMode ? 1231 : 1237);
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
        if (this.batchMode != other.batchMode) {
            return false;
        }
        if (this.bufferSize != other.bufferSize) {
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
        if (this.profile != other.profile) {
            return false;
        }
        if (this.threadsNb != other.threadsNb) {
            return false;
        }
        if (this.timeout != other.timeout) {
            return false;
        }
        if (this.verboseMode != other.verboseMode) {
            return false;
        }
        if (this.warmupMode != other.warmupMode) {
            return false;
        }
        return true;
    }

}