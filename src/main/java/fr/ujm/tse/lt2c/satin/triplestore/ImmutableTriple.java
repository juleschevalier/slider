package fr.ujm.tse.lt2c.satin.triplestore;

import fr.ujm.tse.lt2c.satin.interfaces.Triple;



/**
 * Immutable implementation of triple
 * 
 * @author Jules Chevalier
 *
 */
public final class ImmutableTriple implements Triple {

	private final long subject;
	private final long predicate;
	private final long object;
	
	public ImmutableTriple(long subject, long predicate, long object) {
		super();
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}

	/* (non-Javadoc)
	 * @see fr.ujm.tse.lt2c.satin.Triple#getSubject()
	 */
	@Override
	public long getSubject() {
		return subject;
	}
	/* (non-Javadoc)
	 * @see fr.ujm.tse.lt2c.satin.Triple#getPredicate()
	 */
	@Override
	public long getPredicate() {
		return predicate;
	}
	/* (non-Javadoc)
	 * @see fr.ujm.tse.lt2c.satin.Triple#getObject()
	 */
	@Override
	public long getObject() {
		return object;
	}
	/* (non-Javadoc)
	 * @see fr.ujm.tse.lt2c.satin.Triple#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (object ^ (object >>> 32));
		result = prime * result + (int) (predicate ^ (predicate >>> 32));
		result = prime * result + (int) (subject ^ (subject >>> 32));
		return result;
	}
	/* (non-Javadoc)
	 * @see fr.ujm.tse.lt2c.satin.Triple#equals(Object obj)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImmutableTriple other = (ImmutableTriple) obj;
		if (object != other.object)
			return false;
		if (predicate != other.predicate)
			return false;
		if (subject != other.subject)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "TripleImplNaive [subject=" + subject + ", predicate="
				+ predicate + ", object=" + object + "]";
	}	
	
}
