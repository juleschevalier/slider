package fr.ujm.tse.lt2c.satin.tools;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity(noClassnameStored = true)
public class RunEntity {

	@Id
	private ObjectId id;
	private int sessionId;
	private String file;
	private Date date;
	private String loops;
	private String nbDuplicates;
	private String inferenceTime;
	private String nbInitialTriples;
	private String nbInferedTriples;
	private String nbMissingTriples;
	private String nbTooTriples;

	@Embedded
	private List<String> missingTriples;
	@Embedded
	private List<String> tooTriples;

	public RunEntity() {
		super();
	}

	public RunEntity(String file, int sessionId, long loops, int nbDuplicates, long inferenceTime, long nbInitialTriples, long nb_infered_triples, List<String> missing_triples, List<String> too_triples) {
		super();
		this.sessionId = sessionId;
		this.file = file;
		this.date = new Date();
		this.loops = loops + "";
		this.nbDuplicates = nbDuplicates + "";
		this.inferenceTime = inferenceTime + "";
		this.nbInitialTriples = nbInitialTriples + "";
		this.nbInferedTriples = nb_infered_triples + "";
		this.nbMissingTriples = missing_triples.size() + "";
		this.nbTooTriples = too_triples.size() + "";
		this.missingTriples = missing_triples;
		this.tooTriples = too_triples;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public int getSessionId() {
		return sessionId;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getLoops() {
		return loops;
	}

	public void setLoops(String loops) {
		this.loops = loops;
	}

	public String getNbDuplicates() {
		return nbDuplicates;
	}

	public void setNbDuplicates(String nbDuplicates) {
		this.nbDuplicates = nbDuplicates;
	}

	public String getInferenceTime() {
		return inferenceTime;
	}

	public void setInferenceTime(String inferenceTime) {
		this.inferenceTime = inferenceTime;
	}

	public String getNbInitialTriples() {
		return nbInitialTriples;
	}

	public void setNbInitialTriples(String nbInitialTriples) {
		this.nbInitialTriples = nbInitialTriples;
	}

	public String getNbInferedTriples() {
		return nbInferedTriples;
	}

	public void setNbInferedTriples(String nbInferedTriples) {
		this.nbInferedTriples = nbInferedTriples;
	}

	public String getNbMissingTriples() {
		return nbMissingTriples;
	}

	public void setNbMissingTriples(String nbMissingTriples) {
		this.nbMissingTriples = nbMissingTriples;
	}

	public String getNbTooTriples() {
		return nbTooTriples;
	}

	public void setNbTooTriples(String nbTooTriples) {
		this.nbTooTriples = nbTooTriples;
	}

	public List<String> getMissingTriples() {
		return missingTriples;
	}

	public void setMissingTriples(List<String> missingTriples) {
		this.missingTriples = missingTriples;
	}

	public List<String> getTooTriples() {
		return tooTriples;
	}

	public void setTooTriples(List<String> tooTriples) {
		this.tooTriples = tooTriples;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RunEntity [id=");
		builder.append(id);
		builder.append(", session_id=");
		builder.append(sessionId);
		builder.append(", file=");
		builder.append(file);
		builder.append(", date=");
		builder.append(date);
		builder.append(", loops=");
		builder.append(loops);
		builder.append(", nb_duplicates=");
		builder.append(nbDuplicates);
		builder.append(", inference_time=");
		builder.append(inferenceTime);
		builder.append(", nb_initial_triples=");
		builder.append(nbInitialTriples);
		builder.append(", nb_infered_triples=");
		builder.append(nbInferedTriples);
		builder.append(", nb_missing_triples=");
		builder.append(nbMissingTriples);
		builder.append(", nb_too_triples=");
		builder.append(nbTooTriples);
		builder.append(", missing_triples=");
		builder.append(missingTriples);
		builder.append(", too_triples=");
		builder.append(tooTriples);
		builder.append("]");
		return builder.toString();
	}

}
