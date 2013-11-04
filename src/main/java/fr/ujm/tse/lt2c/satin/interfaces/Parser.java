package fr.ujm.tse.lt2c.satin.interfaces;

/**
 * @author jules
 * 
 * Interface for an ontology parser
 */
public interface Parser {

	/**
	 * @param fileInput The ontology
	 * Parse and "save" the ontology's triples
	 */
	public abstract void parse(String fileInput);
	
	public int hashCode();
	
	public boolean equals(Object obj);

}