package fr.ujm.tse.lt2c.satin.utils;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity(noClassnameStored = true)
public class RunEntity {

    /* Mongo stuff */
    @Id
    private ObjectId id;

    /* Config stuff */
    private String machineName;
    private int coresNb;
    private long ram;
    private int threadsNb;
    private long bufferSize;
    private String version;

    /* Results */
    private int sessionId;
    private String fileInput;
    private Date date;
    private long parsingTime;
    private long inferenceTime;
    private long nbInitialTriples;
    private long nbInferedTriples;
    private String perfStat;

    @Embedded
    private Map<String, AtomicLong> runsByRule;
    @Embedded
    private Map<String, AtomicLong> duplicatesByRule;
    @Embedded
    private Map<String, AtomicLong> inferedByRule;

    public RunEntity() {
        super();
    }

    public RunEntity(final String machineName, final int coresNb, final long ram, final int threadsNb, final long bufferSize, final String version,
            final int sessionId, final String fileInput, final Date date, final long parsingTime, final long inferenceTime, final long nbInitialTriples,
            final long nbInferedTriples, final String perfStat, final Map<String, AtomicLong> runsByRule, final Map<String, AtomicLong> duplicatesByRule,
            final Map<String, AtomicLong> inferedByRule) {
        super();
        this.machineName = machineName;
        this.coresNb = coresNb;
        this.ram = ram;
        this.threadsNb = threadsNb;
        this.bufferSize = bufferSize;
        this.version = version;
        this.sessionId = sessionId;
        this.fileInput = fileInput;
        this.date = date;
        this.parsingTime = parsingTime;
        this.inferenceTime = inferenceTime;
        this.nbInitialTriples = nbInitialTriples;
        this.nbInferedTriples = nbInferedTriples;
        this.perfStat = perfStat;
        this.runsByRule = runsByRule;
        this.duplicatesByRule = duplicatesByRule;
        this.inferedByRule = inferedByRule;
    }

    public ObjectId getId() {
        return this.id;
    }

    public void setId(final ObjectId id) {
        this.id = id;
    }

    public String getMachineName() {
        return this.machineName;
    }

    public void setMachineName(final String machineName) {
        this.machineName = machineName;
    }

    public int getCoresNb() {
        return this.coresNb;
    }

    public void setCoresNb(final int coresNb) {
        this.coresNb = coresNb;
    }

    public long getRam() {
        return this.ram;
    }

    public void setRam(final int ram) {
        this.ram = ram;
    }

    public int getThreadsNb() {
        return this.threadsNb;
    }

    public void setThreadsNb(final int threadsNb) {
        this.threadsNb = threadsNb;
    }

    public long getBufferSize() {
        return this.bufferSize;
    }

    public void setBufferSize(final int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public int getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(final int sessionId) {
        this.sessionId = sessionId;
    }

    public String getFileInput() {
        return this.fileInput;
    }

    public void setFileInput(final String fileInput) {
        this.fileInput = fileInput;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public long getParsingTime() {
        return this.parsingTime;
    }

    public void setParsingTime(final long parsingTime) {
        this.parsingTime = parsingTime;
    }

    public long getInferenceTime() {
        return this.inferenceTime;
    }

    public void setInferenceTime(final long inferenceTime) {
        this.inferenceTime = inferenceTime;
    }

    public long getNbInitialTriples() {
        return this.nbInitialTriples;
    }

    public void setNbInitialTriples(final long nbInitialTriples) {
        this.nbInitialTriples = nbInitialTriples;
    }

    public long getNbInferedTriples() {
        return this.nbInferedTriples;
    }

    public void setNbInferedTriples(final long nbInferedTriples) {
        this.nbInferedTriples = nbInferedTriples;
    }

    public String getPerfStat() {
        return this.perfStat;
    }

    public void setPerfStat(final String perfStat) {
        this.perfStat = perfStat;
    }

    public Map<String, AtomicLong> getRunsByRule() {
        return this.runsByRule;
    }

    public void setRunsByRule(final Map<String, AtomicLong> runsByRule) {
        this.runsByRule = runsByRule;
    }

    public Map<String, AtomicLong> getDuplicatesByRule() {
        return this.duplicatesByRule;
    }

    public void setDuplicatesByRule(final Map<String, AtomicLong> duplicatesByRule) {
        this.duplicatesByRule = duplicatesByRule;
    }

    public Map<String, AtomicLong> getInferedByRule() {
        return this.inferedByRule;
    }

    public void setInferedByRule(final Map<String, AtomicLong> inferedByRule) {
        this.inferedByRule = inferedByRule;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (int) (this.bufferSize ^ (this.bufferSize >>> 32));
        result = (prime * result) + this.coresNb;
        result = (prime * result) + ((this.date == null) ? 0 : this.date.hashCode());
        result = (prime * result) + ((this.duplicatesByRule == null) ? 0 : this.duplicatesByRule.hashCode());
        result = (prime * result) + ((this.fileInput == null) ? 0 : this.fileInput.hashCode());
        result = (prime * result) + ((this.id == null) ? 0 : this.id.hashCode());
        result = (prime * result) + ((this.inferedByRule == null) ? 0 : this.inferedByRule.hashCode());
        result = (prime * result) + (int) (this.inferenceTime ^ (this.inferenceTime >>> 32));
        result = (prime * result) + ((this.machineName == null) ? 0 : this.machineName.hashCode());
        result = (prime * result) + (int) (this.nbInferedTriples ^ (this.nbInferedTriples >>> 32));
        result = (prime * result) + (int) (this.nbInitialTriples ^ (this.nbInitialTriples >>> 32));
        result = (prime * result) + (int) (this.parsingTime ^ (this.parsingTime >>> 32));
        result = (prime * result) + ((this.perfStat == null) ? 0 : this.perfStat.hashCode());
        result = (prime * result) + (int) (this.ram ^ (this.ram >>> 32));
        result = (prime * result) + ((this.runsByRule == null) ? 0 : this.runsByRule.hashCode());
        result = (prime * result) + this.sessionId;
        result = (prime * result) + this.threadsNb;
        result = (prime * result) + ((this.version == null) ? 0 : this.version.hashCode());
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
        final RunEntity other = (RunEntity) obj;
        if (this.bufferSize != other.bufferSize) {
            return false;
        }
        if (this.coresNb != other.coresNb) {
            return false;
        }
        if (this.date == null) {
            if (other.date != null) {
                return false;
            }
        } else if (!this.date.equals(other.date)) {
            return false;
        }
        if (this.duplicatesByRule == null) {
            if (other.duplicatesByRule != null) {
                return false;
            }
        } else if (!this.duplicatesByRule.equals(other.duplicatesByRule)) {
            return false;
        }
        if (this.fileInput == null) {
            if (other.fileInput != null) {
                return false;
            }
        } else if (!this.fileInput.equals(other.fileInput)) {
            return false;
        }
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        if (this.inferedByRule == null) {
            if (other.inferedByRule != null) {
                return false;
            }
        } else if (!this.inferedByRule.equals(other.inferedByRule)) {
            return false;
        }
        if (this.inferenceTime != other.inferenceTime) {
            return false;
        }
        if (this.machineName == null) {
            if (other.machineName != null) {
                return false;
            }
        } else if (!this.machineName.equals(other.machineName)) {
            return false;
        }
        if (this.nbInferedTriples != other.nbInferedTriples) {
            return false;
        }
        if (this.nbInitialTriples != other.nbInitialTriples) {
            return false;
        }
        if (this.parsingTime != other.parsingTime) {
            return false;
        }
        if (this.perfStat == null) {
            if (other.perfStat != null) {
                return false;
            }
        } else if (!this.perfStat.equals(other.perfStat)) {
            return false;
        }
        if (this.ram != other.ram) {
            return false;
        }
        if (this.runsByRule == null) {
            if (other.runsByRule != null) {
                return false;
            }
        } else if (!this.runsByRule.equals(other.runsByRule)) {
            return false;
        }
        if (this.sessionId != other.sessionId) {
            return false;
        }
        if (this.threadsNb != other.threadsNb) {
            return false;
        }
        if (this.version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!this.version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("RunEntity [id=");
        builder.append(this.id);
        builder.append("\n machineName=");
        builder.append(this.machineName);
        builder.append("\n coresNb=");
        builder.append(this.coresNb);
        builder.append("\n ram=");
        builder.append(this.ram);
        builder.append("\n threadsNb=");
        builder.append(this.threadsNb);
        builder.append("\n bufferSize=");
        builder.append(this.bufferSize);
        builder.append("\n version=");
        builder.append(this.version);
        builder.append("\n sessionId=");
        builder.append(this.sessionId);
        builder.append("\n fileInput=");
        builder.append(this.fileInput);
        builder.append("\n date=");
        builder.append(this.date);
        builder.append("\n parsingTime=");
        builder.append(this.parsingTime);
        builder.append("\n inferenceTime=");
        builder.append(this.inferenceTime);
        builder.append("\n nbInitialTriples=");
        builder.append(this.nbInitialTriples);
        builder.append("\n nbInferedTriples=");
        builder.append(this.nbInferedTriples);
        builder.append("\n perfStat=");
        builder.append(this.perfStat);
        builder.append("\n runsByRule=");
        builder.append(this.runsByRule);
        builder.append("\n duplicatesByRule=");
        builder.append(this.duplicatesByRule);
        builder.append("\n inferedByRule=");
        builder.append(this.inferedByRule);
        builder.append("]");
        return builder.toString();
    }

}
