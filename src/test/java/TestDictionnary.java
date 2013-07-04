
import org.junit.Assert;
import org.junit.Test;

import fr.ujm.tse.lt2c.satin.naiveImpl.DictionnaryImplNaive;

public class TestDictionnary {

	@Test
	public void testSize(){
		
		DictionnaryImplNaive dico = new DictionnaryImplNaive();

		Assert.assertEquals(0, dico.size());
		
		String s = "Hello world";
		dico.add(s);
		
		Assert.assertEquals(1, dico.size());
		
	}

	@Test
	public void testAddGet(){
		
		DictionnaryImplNaive dico = new DictionnaryImplNaive();
		
		String s1 = "Hello world";
		long l1 = dico.add(s1);
		
		Assert.assertEquals(l1, dico.get(s1));
		Assert.assertEquals(s1, dico.get(l1));
		
		long l2 = dico.add(s1);
		Assert.assertEquals(l1, l2);
		
		String s2 = "Goodbye world";
		long l3 = dico.add(s2);
		Assert.assertNotSame(l1, l3);
		
	}
	
}
