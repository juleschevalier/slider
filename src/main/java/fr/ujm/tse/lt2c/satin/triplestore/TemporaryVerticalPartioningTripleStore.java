package fr.ujm.tse.lt2c.satin.triplestore;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;

/**
 * 
 * @author Julien Subercaze
 * 
 *         Triple Store that implements vertical partioning approach
 */
public class TemporaryVerticalPartioningTripleStore extends VerticalPartioningTripleStore{
	
	ArrayList<Triple> triplesCollection;

	public TemporaryVerticalPartioningTripleStore() {
		super();
		triplesCollection = new ArrayList<>();
	}

	@Override
	public void add(Triple t) {
		super.add(t);
		this.triplesCollection.add(t);
	}

	@Override
	public void addAll(Collection<Triple> t) {
		for (Triple triple : t) {
			add(triple);
		}

	}

	@Override
	public Collection<Triple> getAll() {
		return this.triplesCollection;
	}

	@Override
	public void writeToFile(String file, Dictionnary dictionnary) {
		try {
			// Create file
			FileWriter fstream = new FileWriter(file, false);
			BufferedWriter out = new BufferedWriter(fstream);
			for (Triple triple : this.triplesCollection) {
				out.write(dictionnary.printTriple(triple) + "\n");
			}
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

	}

	public void clear() {
		this.internalstore.clear();
		this.triples = 0;
		this.triplesCollection.clear();
		
	}

}
