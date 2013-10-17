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
	private int session_id;
	private String file;
	private Date date;
	private String loops;
	private String nb_duplicates;
	private String inference_time;
	private String nb_initial_triples;
	private String nb_infered_triples;
	private String nb_missing_triples;
	private String nb_too_triples;

	@Embedded
	private List<String> missing_triples;
	@Embedded
	private List<String> too_triples;

	public RunEntity(String file, int session_id, long loops, int nb_duplicates, long inference_time, long nb_initial_triples, long nb_infered_triples, List<String> missing_triples, List<String> too_triples) {
		super();
		this.setSession_id(session_id);
		this.file = file;
		this.date = new Date();
		this.loops = loops+"";
		this.nb_duplicates = nb_duplicates+"";
		this.inference_time = inference_time+"";
		this.nb_initial_triples = nb_initial_triples+"";
		this.nb_infered_triples = nb_infered_triples+"";
		this.nb_missing_triples = missing_triples.size()+"";
		this.nb_too_triples = too_triples.size()+"";
		this.missing_triples = missing_triples;
		this.too_triples = too_triples;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
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

	public String getInference_time() {
		return inference_time;
	}

	public void setInference_time(String inference_time) {
		this.inference_time = inference_time;
	}

	public String getNb_initial_triples() {
		return nb_initial_triples;
	}

	public void setNb_initial_triples(String nb_initial_triples) {
		this.nb_initial_triples = nb_initial_triples;
	}

	public String getNb_infered_triples() {
		return nb_infered_triples;
	}

	public void setNb_infered_triples(String nb_infered_triples) {
		this.nb_infered_triples = nb_infered_triples;
	}

	public String getNb_missing_triples() {
		return nb_missing_triples;
	}

	public void setNb_missing_triples(String nb_missing_triples) {
		this.nb_missing_triples = nb_missing_triples;
	}

	public String getNb_too_triples() {
		return nb_too_triples;
	}

	public void setNb_too_triples(String nb_too_triples) {
		this.nb_too_triples = nb_too_triples;
	}

	public List<String> getMissing_triples() {
		return missing_triples;
	}

	public void setMissing_triples(List<String> missing_triples) {
		this.missing_triples = missing_triples;
	}

	public List<String> getToo_triples() {
		return too_triples;
	}

	public void setToo_triples(List<String> too_triples) {
		this.too_triples = too_triples;
	}

	public int getSession_id() {
		return session_id;
	}

	public void setSession_id(int session_id) {
		this.session_id = session_id;
	}

	public String getNb_duplicates() {
		return nb_duplicates;
	}

	public void setNb_duplicates(String nb_duplicates) {
		this.nb_duplicates = nb_duplicates;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RunEntity [id=");
		builder.append(id);
		builder.append(", session_id=");
		builder.append(session_id);
		builder.append(", file=");
		builder.append(file);
		builder.append(", date=");
		builder.append(date);
		builder.append(", loops=");
		builder.append(loops);
		builder.append(", nb_duplicates=");
		builder.append(nb_duplicates);
		builder.append(", inference_time=");
		builder.append(inference_time);
		builder.append(", nb_initial_triples=");
		builder.append(nb_initial_triples);
		builder.append(", nb_infered_triples=");
		builder.append(nb_infered_triples);
		builder.append(", nb_missing_triples=");
		builder.append(nb_missing_triples);
		builder.append(", nb_too_triples=");
		builder.append(nb_too_triples);
		builder.append(", missing_triples=");
		builder.append(missing_triples);
		builder.append(", too_triples=");
		builder.append(too_triples);
		builder.append("]");
		return builder.toString();
	}
	
	

}
