package fr.ujm.tse.lt2c.satin.slider.utils;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Jules Chevalier
 *
 */
public final class GlobalValues {

    private static Map<String, AtomicLong> runsByRule;
    private static Map<String, AtomicLong> duplicatesByRule;
    private static Map<String, AtomicLong> inferedByRule;
    private static Map<String, AtomicLong> timeoutByRule;
    private static Map<String, Long> timeByFile;
    private static Map<Long, Map<Long, Long>> timestampInferedPredicates; // <predicate,[timestamp,amount]>
    private static long startTime;

    private GlobalValues() {

    }

    static {
        runsByRule = new HashMap<String, AtomicLong>();
        duplicatesByRule = new HashMap<String, AtomicLong>();
        inferedByRule = new HashMap<String, AtomicLong>();
        timeoutByRule = new HashMap<String, AtomicLong>();
        timeByFile = new HashMap<String, Long>();
        timestampInferedPredicates = new HashMap<>();
        startTime = System.nanoTime();
    }

    public static void reset() {
        runsByRule = new HashMap<String, AtomicLong>();
        duplicatesByRule = new HashMap<String, AtomicLong>();
        inferedByRule = new HashMap<String, AtomicLong>();
        timeoutByRule = new HashMap<String, AtomicLong>();
        timestampInferedPredicates = new HashMap<>();
        startTime = System.nanoTime();
    }

    public static void incRunsByRule(final String rule) {
        synchronized (runsByRule) {
            if (!runsByRule.containsKey(rule)) {
                runsByRule.put(rule, new AtomicLong(1));
            } else {
                runsByRule.get(rule).incrementAndGet();
            }
        }
    }

    public static void incTimeoutByRule(final String rule) {
        synchronized (timeoutByRule) {
            if (!timeoutByRule.containsKey(rule)) {
                timeoutByRule.put(rule, new AtomicLong(1));
            } else {
                timeoutByRule.get(rule).incrementAndGet();
            }
        }
    }

    public static void incDuplicatesByRule(final String rule, final long number) {
        synchronized (duplicatesByRule) {
            if (!duplicatesByRule.containsKey(rule)) {
                duplicatesByRule.put(rule, new AtomicLong(number));
            } else {
                duplicatesByRule.get(rule).addAndGet(number);
            }
        }
    }

    public static void incInferedByRule(final String rule, final long number) {
        synchronized (inferedByRule) {
            if (!inferedByRule.containsKey(rule)) {
                inferedByRule.put(rule, new AtomicLong(number));
            } else {
                inferedByRule.get(rule).addAndGet(number);
            }
        }
    }

    public static void addTimeForFile(final String file, final long time) {
        synchronized (timeByFile) {
            if (!timeByFile.containsKey(file)) {
                timeByFile.put(file, time);
            } else {
                final long newTime = (timeByFile.get(file) + time) / 2;
                timeByFile.put(file, newTime);
            }
        }

    }

    public static void addTimestampPredicate(final long predicate, long timestamp, final long amount) {
        synchronized (timestampInferedPredicates) {
            timestamp = (timestamp - startTime) / 100_000_000;
            if (!timestampInferedPredicates.containsKey(predicate)) {
                final Map<Long, Long> tmp = new HashMap<Long, Long>();
                tmp.put(timestamp, amount);
                timestampInferedPredicates.put(predicate, tmp);
            } else {
                final Map<Long, Long> tmp = timestampInferedPredicates.get(predicate);
                if (!tmp.containsKey(timestamp)) {
                    tmp.put(timestamp, amount);
                } else {
                    tmp.put(timestamp, amount + tmp.get(timestamp));
                }
            }
        }
    }

    public static Map<String, AtomicLong> getRunsByRule() {
        synchronized (runsByRule) {
            return runsByRule;
        }
    }

    public static Map<String, AtomicLong> getDuplicatesByRule() {
        synchronized (duplicatesByRule) {
            return duplicatesByRule;
        }
    }

    public static Map<String, AtomicLong> getInferedByRule() {
        synchronized (inferedByRule) {
            return inferedByRule;
        }
    }

    public static Map<String, Long> getTimeByFile() {
        synchronized (timeByFile) {
            return timeByFile;
        }
    }

    public static Map<String, AtomicLong> getTimeoutByRule() {
        synchronized (timeoutByRule) {
            return timeoutByRule;
        }
    }

    public static Collection<Long[]> getTimestampInferedPredicates() {
        synchronized (timestampInferedPredicates) {
            final Collection<Long[]> result = new ArrayList<Long[]>();
            for (final Long predicate : timestampInferedPredicates.keySet()) {
                for (final Long timestamp : timestampInferedPredicates.get(predicate).keySet()) {
                    result.add(new Long[] { predicate, timestamp, timestampInferedPredicates.get(predicate).get(timestamp) });
                }
            }
            return result;
        }
    }
}
