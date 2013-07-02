package fr.ujm.tse.lt2c.satin;

/**
 * @author Jules Chevalier
 * Interface for triple representation
 *
 */

public interface Triple {

	public abstract long getSubject();

	public abstract void setSubject(long subject);

	public abstract long getPredicate();

	public abstract void setPredicate(long predicate);

	public abstract long getObject();

	public abstract void setObject(long object);

	public abstract int hashCode();

	public abstract boolean equals(Object obj);

}