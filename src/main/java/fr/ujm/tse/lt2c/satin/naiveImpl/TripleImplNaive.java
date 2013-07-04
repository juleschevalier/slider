package fr.ujm.tse.lt2c.satin.naiveImpl;

import fr.ujm.tse.lt2c.satin.interfaces.Triple;



/**
 * Naive implementation of triple
 * 
 * @author Jules Chevalier
 *
 */
public class TripleImplNaive implements Triple {

	long subject;
	long predicate;
	long object;
	public TripleImplNaive(long subject, long predicate, long object) {
		super();
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}
	public TripleImplNaive() {
	}
	/* (non-Javadoc)
	 * @see fr.ujm.tse.lt2c.satin.Triple#getSubject()
	 */
	@Override
	public long getSubject() {
		return subject;
	}
	/* (non-Javadoc)
	 * @see fr.ujm.tse.lt2c.satin.Triple#setSubject(long)
	 */
	@Override
	public void setSubject(long subject) {
		this.subject = subject;
	}
	/* (non-Javadoc)
	 * @see fr.ujm.tse.lt2c.satin.Triple#getPredicate()
	 */
	@Override
	public long getPredicate() {
		return predicate;
	}
	/* (non-Javadoc)
	 * @see fr.ujm.tse.lt2c.satin.Triple#setPredicate(long)
	 */
	@Override
	public void setPredicate(long predicate) {
		this.predicate = predicate;
	}
	/* (non-Javadoc)
	 * @see fr.ujm.tse.lt2c.satin.Triple#getObject()
	 */
	@Override
	public long getObject() {
		return object;
	}
	/* (non-Javadoc)
	 * @see fr.ujm.tse.lt2c.satin.Triple#setObject(long)
	 */
	@Override
	public void setObject(long object) {
		this.object = object;
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
	 * @see fr.ujm.tse.lt2c.satin.Triple#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TripleImplNaive other = (TripleImplNaive) obj;
		if (object != other.object)
			return false;
		if (predicate != other.predicate)
			return false;
		if (subject != other.subject)
			return false;
		return true;
	}
}
