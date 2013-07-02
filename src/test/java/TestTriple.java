
import org.junit.Assert;
import org.junit.Test;

import fr.ujm.tse.lt2c.satin.Triple;
import fr.ujm.tse.lt2c.satin.TripleImplNaive;

public class TestTriple {

	@Test
	public void testSetters(){
		long s=1l,p=2l,o=3l;
		
		Triple t1 = new TripleImplNaive(s, p, o);
		Triple t2 = new TripleImplNaive();
		
		t2.setSubject(s);
		t2.setPredicate(p);
		t2.setObject(o);
		
		Assert.assertEquals(t1,t2);
	}

	@Test
	public void testGetters(){
		long s1=1l,p1=2l,o1=3l;
		long s2,p2,o2;

		Triple t = new TripleImplNaive(s1, p1, o1);
		
		s2=t.getSubject();
		p2=t.getPredicate();
		o2=t.getObject();
		
		Assert.assertEquals(s1,s2);
		Assert.assertEquals(p1,p2);
		Assert.assertEquals(o1,o2);
	}
}
