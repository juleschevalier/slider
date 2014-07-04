package fr.ujm.tse.lt2c.satin.buffer;

import java.util.HashMap;
import java.util.TimerTask;

import fr.ujm.tse.lt2c.satin.rules.Rule;

public class BufferTimer extends TimerTask {

    private final HashMap<Rule, Long> rulesLastFlushes;
    private final long timeout;

    public BufferTimer(final long timeout) {
        super();
        this.rulesLastFlushes = new HashMap<>();
        this.timeout = timeout;
    }

    @Override
    public void run() {
        final Long now = System.nanoTime();
        Long lastAdd;
        for (final Rule rule : this.rulesLastFlushes.keySet()) {
            lastAdd = (now - this.rulesLastFlushes.get(rule)) / 1000000;
            if (lastAdd > this.timeout && rule.getTripleBuffer().getOccupation() > 0) {
                rule.bufferFull();
            }
        }

    }

    public void notifyAdd(final Rule rule) {
        final long now = System.nanoTime();
        this.rulesLastFlushes.put(rule, now);
    }

    public void addRule(final Rule rule) {
        this.notifyAdd(rule);
    }
}
