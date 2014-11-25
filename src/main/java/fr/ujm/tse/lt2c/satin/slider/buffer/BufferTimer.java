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

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import fr.ujm.tse.lt2c.satin.slider.rules.Rule;

/**
 * @author Jules Chevalier
 *
 */
public class BufferTimer extends TimerTask {

    public static final long DEFAULT_TIMEOUT = 500;

    private final Map<Rule, Long> rulesLastAdd;
    private final Map<String, Boolean> rulesActivated;
    private final long timeout;

    public BufferTimer(final long timeout) {
        super();
        this.rulesLastAdd = new HashMap<>();
        this.rulesActivated = new HashMap<>();
        if (timeout <= 0) {
            throw new RuntimeException("Timeout=0!");
        }
        this.timeout = timeout;
    }

    @Override
    public void run() {
        final Long now = System.nanoTime();
        final int nsToMs = 1_000_000;
        Long lastAdd;
        for (final Rule rule : this.rulesLastAdd.keySet()) {
            lastAdd = (now - this.rulesLastAdd.get(rule)) / nsToMs;
            if (!this.isActivated(rule.name()) && lastAdd > this.timeout && rule.getTripleBuffer().getOccupation() > 0
                    && rule.getTripleBuffer().size() < rule.getTripleBuffer().getBufferLimit()) {
                this.rulesActivated.put(rule.name(), true);
                rule.bufferFullTimer(rule.getTripleBuffer().getOccupation());
                rule.getTripleBuffer().timerCall(rule.getTripleBuffer().getOccupation());
            }
        }

    }

    private boolean isActivated(final String rule) {
        return this.rulesActivated.containsKey(rule) && this.rulesActivated.get(rule);
    }

    public void notifyAdd(final Rule rule) {
        final long now = System.nanoTime();
        this.rulesLastAdd.put(rule, now);
    }

    public void deactivateRule(final String rule) {
        this.rulesActivated.put(rule, false);
    }

    public void addRule(final Rule rule) {
        this.notifyAdd(rule);
    }
}
