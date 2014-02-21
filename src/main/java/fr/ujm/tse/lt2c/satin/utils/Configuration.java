package fr.ujm.tse.lt2c.satin.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Configuration {

    private static Map<String, AtomicLong> runsByRule;
    private static Map<String, AtomicLong> duplicatesByRule;
    private static Map<String, AtomicLong> inferedByRule;

    static {
        runsByRule = new HashMap<String, AtomicLong>();
        duplicatesByRule = new HashMap<String, AtomicLong>();
        inferedByRule = new HashMap<String, AtomicLong>();
    }

    public static void reset() {
        runsByRule = new HashMap<String, AtomicLong>();
        duplicatesByRule = new HashMap<String, AtomicLong>();
        inferedByRule = new HashMap<String, AtomicLong>();
    }

    public static void incRunsByRule(final String rule) {
        if (!runsByRule.containsKey(rule)) {
            runsByRule.put(rule, new AtomicLong(1));
        } else {
            runsByRule.get(rule).incrementAndGet();
        }
    }

    public static void incDuplicatesByRule(final String rule, final long number) {
        if (!duplicatesByRule.containsKey(rule)) {
            duplicatesByRule.put(rule, new AtomicLong(number));
        } else {
            duplicatesByRule.get(rule).addAndGet(number);
        }
    }

    public static void incInferedByRule(final String rule, final long number) {
        if (!inferedByRule.containsKey(rule)) {
            inferedByRule.put(rule, new AtomicLong(number));
        } else {
            inferedByRule.get(rule).addAndGet(number);
        }
    }

    public static Map<String, AtomicLong> getRunsByRule() {
        return runsByRule;
    }

    public static Map<String, AtomicLong> getDuplicatesByRule() {
        return duplicatesByRule;
    }

    public static Map<String, AtomicLong> getInferedByRule() {
        return inferedByRule;
    }

}
