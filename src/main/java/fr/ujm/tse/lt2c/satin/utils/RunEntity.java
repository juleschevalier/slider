package fr.ujm.tse.lt2c.satin.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity(noClassnameStored = true)
public class RunEntity {

    /* Mongo */
    @Id
    private ObjectId id;

    /* Environment */
    private String machineName;
    private int coresNb;
    private long ram;

    /* Configuration */
    private int threadsNb;
    private long bufferSize;
    private long timeout;
    private String version;
    private String profile;
    @Embedded
    private Collection<String> rules;

    /* Results */
    private int sessionId;
    private String fileInput;
    private Date date;
    private long parsingTime;
    private long inferenceTime;
    private long nbInitialTriples;
    private long nbInferedTriples;

    @Embedded
    private Map<String, AtomicLong> runsByRule;
    @Embedded
    private Map<String, AtomicLong> duplicatesByRule;
    @Embedded
    private Map<String, AtomicLong> inferedByRule;
    @Embedded
    private Map<String, AtomicLong> timeoutByRule;

    public RunEntity() {
        super();
    }

    public RunEntity(final int threadsNb, final long bufferSize, final long timeout, final String version, final String profile,
            final Collection<String> rules, final int sessionId, final String fileInput, final long parsingTime, final long inferenceTime,
            final long nbInitialTriples, final long nbInferedTriples, final Map<String, AtomicLong> runsByRule, final Map<String, AtomicLong> duplicatesByRule,
            final Map<String, AtomicLong> inferedByRule, final Map<String, AtomicLong> timeoutByRule) {
        super();

        this.machineName = "";
        try {
            this.machineName = InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException e) {}
        this.machineName += " " + System.getProperty("os.name") + " " + System.getProperty("os.version") + "(" + System.getProperty("os.arch") + ")";
        this.coresNb = Runtime.getRuntime().availableProcessors();
        this.ram = Runtime.getRuntime().totalMemory();
        this.date = new Date();

        this.threadsNb = threadsNb;
        this.bufferSize = bufferSize;
        this.timeout = timeout;
        this.version = version;
        this.profile = profile;
        this.rules = rules;
        this.sessionId = sessionId;
        this.fileInput = fileInput;
        this.parsingTime = parsingTime;
        this.inferenceTime = inferenceTime;
        this.nbInitialTriples = nbInitialTriples;
        this.nbInferedTriples = nbInferedTriples;
        this.runsByRule = runsByRule;
        this.duplicatesByRule = duplicatesByRule;
        this.inferedByRule = inferedByRule;
        this.timeoutByRule = timeoutByRule;
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

    public void setRam(final long ram) {
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

    public void setBufferSize(final long bufferSize) {
        this.bufferSize = bufferSize;
    }

    public long getTimeout() {
        return this.timeout;
    }

    public void setTimeout(final long timeout) {
        this.timeout = timeout;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getProfile() {
        return this.profile;
    }

    public void setProfile(final String profile) {
        this.profile = profile;
    }

    public Collection<String> getRules() {
        return this.rules;
    }

    public void setRules(final Collection<String> rules) {
        this.rules = rules;
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

    public Map<String, AtomicLong> getTimeoutByRule() {
        return this.timeoutByRule;
    }

    public void setTimeoutByRule(final Map<String, AtomicLong> timeoutByRule) {
        this.timeoutByRule = timeoutByRule;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.bufferSize ^ this.bufferSize >>> 32);
        result = prime * result + this.coresNb;
        result = prime * result + (this.date == null ? 0 : this.date.hashCode());
        result = prime * result + (this.duplicatesByRule == null ? 0 : this.duplicatesByRule.hashCode());
        result = prime * result + (this.fileInput == null ? 0 : this.fileInput.hashCode());
        result = prime * result + (this.id == null ? 0 : this.id.hashCode());
        result = prime * result + (this.inferedByRule == null ? 0 : this.inferedByRule.hashCode());
        result = prime * result + (int) (this.inferenceTime ^ this.inferenceTime >>> 32);
        result = prime * result + (this.machineName == null ? 0 : this.machineName.hashCode());
        result = prime * result + (int) (this.nbInferedTriples ^ this.nbInferedTriples >>> 32);
        result = prime * result + (int) (this.nbInitialTriples ^ this.nbInitialTriples >>> 32);
        result = prime * result + (int) (this.parsingTime ^ this.parsingTime >>> 32);
        result = prime * result + (this.profile == null ? 0 : this.profile.hashCode());
        result = prime * result + (int) (this.ram ^ this.ram >>> 32);
        result = prime * result + (this.rules == null ? 0 : this.rules.hashCode());
        result = prime * result + (this.runsByRule == null ? 0 : this.runsByRule.hashCode());
        result = prime * result + this.sessionId;
        result = prime * result + this.threadsNb;
        result = prime * result + (int) (this.timeout ^ this.timeout >>> 32);
        result = prime * result + (this.timeoutByRule == null ? 0 : this.timeoutByRule.hashCode());
        result = prime * result + (this.version == null ? 0 : this.version.hashCode());
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
        if (this.profile == null) {
            if (other.profile != null) {
                return false;
            }
        } else if (!this.profile.equals(other.profile)) {
            return false;
        }
        if (this.ram != other.ram) {
            return false;
        }
        if (this.rules == null) {
            if (other.rules != null) {
                return false;
            }
        } else if (!this.rules.equals(other.rules)) {
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
        if (this.timeout != other.timeout) {
            return false;
        }
        if (this.timeoutByRule == null) {
            if (other.timeoutByRule != null) {
                return false;
            }
        } else if (!this.timeoutByRule.equals(other.timeoutByRule)) {
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
        builder.append(", machineName=");
        builder.append(this.machineName);
        builder.append(", coresNb=");
        builder.append(this.coresNb);
        builder.append(", ram=");
        builder.append(this.ram);
        builder.append(", threadsNb=");
        builder.append(this.threadsNb);
        builder.append(", bufferSize=");
        builder.append(this.bufferSize);
        builder.append(", timeout=");
        builder.append(this.timeout);
        builder.append(", version=");
        builder.append(this.version);
        builder.append(", profile=");
        builder.append(this.profile);
        builder.append(", rules={");
        for (final String rule : this.rules) {
            builder.append(rule + " ");
        }
        builder.append("}, sessionId=");
        builder.append(this.sessionId);
        builder.append(", fileInput=");
        builder.append(this.fileInput);
        builder.append(", date=");
        builder.append(this.date);
        builder.append(", parsingTime=");
        builder.append(this.parsingTime);
        builder.append(", inferenceTime=");
        builder.append(this.inferenceTime);
        builder.append(", nbInitialTriples=");
        builder.append(this.nbInitialTriples);
        builder.append(", nbInferedTriples=");
        builder.append(this.nbInferedTriples);
        builder.append(", runsByRule=");
        builder.append(this.runsByRule);
        builder.append(", duplicatesByRule=");
        builder.append(this.duplicatesByRule);
        builder.append(", inferedByRule=");
        builder.append(this.inferedByRule);
        builder.append(", timeoutByRule=");
        builder.append(this.timeoutByRule);
        builder.append("]");
        return builder.toString();
    }

}
