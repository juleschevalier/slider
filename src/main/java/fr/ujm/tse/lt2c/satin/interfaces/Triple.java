package fr.ujm.tse.lt2c.satin.interfaces;

/**
 * @author Jules Chevalier
 * Interface for triple representation
 *
 */

public interface Triple {

	public abstract long getSubject();

	public abstract long getPredicate();

	public abstract long getObject();

	public abstract int hashCode();

	public abstract boolean equals(Object obj);

}