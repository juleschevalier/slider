package fr.ujm.tse.lt2c.satin.slider.buffer;

/*
 * #%L
 * SLIDeR
 * %%
 * Copyright (C) 2014 Université Jean Monnet, Saint Etienne
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.slider.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.slider.rules.RuleModule;

/**
 * @author Jules Chevalier
 *
 */
public class BufferTimer extends TimerTask {

    public static final long DEFAULT_TIMEOUT = 10;

    private static final Logger LOGGER = Logger.getLogger(QueuedTripleBufferLock.class);

    private Long lastAdd;
    private Boolean activated;
    private Dictionary dictionary;
    private long timeout;
    private final RuleModule ruleModule;
    private final Timer timer;

    public BufferTimer(final long timeout, final RuleModule ruleModule) {
        super();
        this.lastAdd = System.nanoTime();
        this.activated = false;
        if (timeout <= 0) {
            throw new IllegalArgumentException("Timeout=0!");
        }
        this.timeout = timeout;
        this.dictionary = null;
        this.ruleModule = ruleModule;
        this.timer = new Timer();
    }

    @Override
    public void run() {
        Thread.currentThread().setName("BTimer-" + this.ruleModule.name());
        final Long now = System.nanoTime();
        final int nsToMs = 1_000_000;
        Long lastAdd;
        long toRead;
        lastAdd = (now - this.lastAdd) / nsToMs;
        if (!this.activated && lastAdd > this.timeout && this.ruleModule.getTripleBuffer().getOccupation() > 0
                && this.ruleModule.getTripleBuffer().size() < this.ruleModule.getTripleBuffer().getBufferLimit()) {
            this.activated = true;
            toRead = this.ruleModule.getTripleBuffer().getOccupation();
            this.ruleModule.getTripleBuffer().timerCall(toRead);
            this.ruleModule.bufferFullTimer(toRead);
        }
        if (this.dictionary != null) {
            synchronized (this.dictionary) {
                this.dictionary.notify();
            }
        }

    }

    public void notifyAdd(final RuleModule ruleModule) {
        this.lastAdd = System.nanoTime();
        if (this.dictionary == null) {
            this.dictionary = ruleModule.getDictionary();
        }
    }

    public void deactivateRule(final String rule) {
        this.activated = false;
    }

    public void addRule(final RuleModule ruleModule) {
        this.notifyAdd(ruleModule);
    }

    public void start() {
        this.timer.scheduleAtFixedRate(this, this.timeout, this.timeout);

    }

    public void stop() {
        this.timer.cancel();
    }

    public void setTimeout(final long timeout) {
        this.timeout = timeout;
        // LOGGER.info(this.ruleModule.name() + " timer " + this.timeout);
    }
}