package fr.ujm.tse.lt2c.satin.rules;

import java.util.concurrent.ExecutorService;

import fr.ujm.tse.lt2c.satin.buffer.TripleBufferLock;
import fr.ujm.tse.lt2c.satin.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.RuleRun;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.rules.run.AbstractRun;


public class Rule implements BufferListener{
	
	TripleBuffer tripleBuffer;
	RuleRun ruleRun;
	ExecutorService executor;
	TripleDistributor distributor;
	
	public Rule(AbstractRun rule, ExecutorService executor) {
		super();
		this.tripleBuffer = new TripleBufferLock();
		this.ruleRun = rule;
		this.executor = executor;
		this.distributor = new TripleDistributor();
		
		this.tripleBuffer.addBufferListener(this);
		
		distributor.subscribe(tripleBuffer, rule.input_matchers);
	}
	
	@Override
	public void bufferFull() {
		this.executor.submit(this.ruleRun);
		//How to pass usable triples to already instantiate rule without corrupt each running threads
	}

}
