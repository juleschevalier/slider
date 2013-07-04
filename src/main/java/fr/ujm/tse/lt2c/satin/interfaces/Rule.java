package fr.ujm.tse.lt2c.satin.interfaces;


public interface Rule {
	
	public void process(TripleStore tripleStore, Dictionnary dictionnary);
	
	public int hashCode();
	
	public boolean equals(Object obj);

}
