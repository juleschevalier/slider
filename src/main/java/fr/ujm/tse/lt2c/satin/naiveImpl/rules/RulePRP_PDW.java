package fr.ujm.tse.lt2c.satin.naiveImpl.rules;

import java.util.Collection;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;

public class RulePRP_PDW implements Rule {

	private static Logger logger = Logger.getLogger(RulePRP_PDW.class);

	@Override
	public void process(TripleStore tripleStore, Dictionnary dictionnary) {


		/**
		 * 	INPUT
		 * p1 owl:propertyDisjointWith p2
		 * x p1 y
		 * x p2 y
		 *  OUPUT
		 * false
		 */

		/*
		 * Get concepts codes in dictionnary
		 */
		long propertyDisjointWith = dictionnary.add("http://www.w3.org/2002/07/owl#propertyDisjointWith");

		/*
		 * Get triples matching input 
		 * Create
		 */
		Collection<Triple> propertyDisjointWith_Triples = tripleStore.getbyPredicate(propertyDisjointWith);

		for (Triple t1 : propertyDisjointWith_Triples) {
			long s1=t1.getSubject(), o1=t1.getObject();

			for (Triple t2 : tripleStore.getAll()) {
				long s2=t2.getSubject(), p2=t2.getPredicate(), o2=t2.getObject();

				for (Triple t3 : tripleStore.getAll()) {
					long s3=t3.getSubject(), p3=t3.getPredicate(), o3=t3.getObject();

					if(s1==p2&&o1==p3&&s2==s3&&o2==o3){
						logger.trace("PRP_PDW "+dictionnary.printTriple(t1)+" + "+dictionnary.printTriple(t2)+" + "+dictionnary.printTriple(t3)+" -> FALSE");
					}
				}
			}
		}
	}

}
