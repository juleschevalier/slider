package fr.ujm.tse.lt2c.satin.rules;

import java.util.concurrent.ExecutorService;

import fr.ujm.tse.lt2c.satin.buffer.TripleBufferLock;
import fr.ujm.tse.lt2c.satin.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.interfaces.BufferListener;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;


public class RuleExecutor implements BufferListener{
	
	TripleBuffer tripleBuffer;
	Rule rule;
	ExecutorService executor;
	public RuleExecutor(AbstractRule rule, TripleDistributor distributor, ExecutorService executor) {
		super();
		this.tripleBuffer = new TripleBufferLock();
		this.rule = rule;
		this.executor = executor;
		
		this.tripleBuffer.addBufferListener(this);
		distributor.subscribe(tripleBuffer, rule.matchers);
	}
	
	@Override
	public void bufferFull() {
		this.executor.submit(this.rule);
		//How to pass usable triples to already instantiate rule without corrupt each running threads
	}

}
