package fr.ujm.tse.lt2c.satin.slider.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.collect.HashMultimap;

public class MonitoredValues {

    private MonitoredValues() {

    }

    private static AtomicLong totalInput;
    private static AtomicLong totalInfered;
    private static String fragment;
    private static long bufferSize;
    private static long timeout;
    private static String fileName;

    private static AtomicLong currentInput;
    private static AtomicLong currentInfered;
    private static AtomicLong runningRules;
    private static AtomicLong waitingRules;

    /* <Rule, occupation> */
    private static Map<String, AtomicLong> buffers;
    /* <Rule, runs> */
    private static Map<String, AtomicLong> runs;
    private static Map<String, AtomicLong> timeouts;
    /* <Rule, inferred> */
    private static Map<String, AtomicLong> inferred;

    private static Queue<String> lastRun;
    private static final byte LAST_RUN_LIMIT = 5;

    private static HashMultimap<String, String> edges;

    private static Collection<JSONObject> jsons;

    private static Collection<String> tic;

    private static Timer timer;
    private static TimerTask timerTask;
    public static Map<String, String> tips;

    static {

        timer = new Timer();
        jsons = new ArrayList<JSONObject>();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                MonitoredValues.saveToJSONObject();
            }
        };

        MonitoredValues.currentInput = new AtomicLong();
        MonitoredValues.currentInfered = new AtomicLong();
        MonitoredValues.runningRules = new AtomicLong();
        MonitoredValues.waitingRules = new AtomicLong();

        MonitoredValues.fragment = "";
        MonitoredValues.bufferSize = 0;
        MonitoredValues.timeout = 0;
        MonitoredValues.fileName = "";

        MonitoredValues.buffers = Collections.synchronizedMap(new HashMap<String, AtomicLong>());
        MonitoredValues.runs = Collections.synchronizedMap(new HashMap<String, AtomicLong>());
        MonitoredValues.timeouts = Collections.synchronizedMap(new HashMap<String, AtomicLong>());
        MonitoredValues.inferred = Collections.synchronizedMap(new HashMap<String, AtomicLong>());
        MonitoredValues.lastRun = new ConcurrentLinkedQueue<>();

        MonitoredValues.edges = HashMultimap.create();

        MonitoredValues.tic = Collections.synchronizedCollection(new HashSet<String>());

        tips = new HashMap<String, String>();
        MonitoredValues.tips.put("CAX_SCO", "<tr><th>CAX_SCO</th><td>c1 subClassOf c2<br> x type c1 </td><td> x type c2</td></tr>");
        MonitoredValues.tips.put("PRP_DOM", "<tr><th>PRP_DOM</th><td>p domain c<br> x p y </td><td> x type c</td></tr>");
        MonitoredValues.tips.put("PRP_RNG", "<tr><th>PRP_RNG</th><td>p range c<br> x p y </td><td> y type c</td></tr>");
        MonitoredValues.tips.put("PRP_SPO1", "<tr><th>PRP_SPO1</th><td>p1 subPropertyOf p2<br> x p1 y </td><td> x p2 y</td></tr>");
        MonitoredValues.tips.put("RDFS10", "<tr><th>RDFS10</th><td>x type Class </td><td> x subClassOf x</td></tr>");
        MonitoredValues.tips.put("RDFS12", "<tr><th>RDFS12</th><td>x type ContainerMembershipProperty </td><td> x subPropertyOf member</td></tr>");
        MonitoredValues.tips.put("RDFS13", "<tr><th>RDFS13</th><td>x type Datatype </td><td> x subClassOf Literal</td></tr>");
        MonitoredValues.tips.put("RDFS4c", "<tr><th>RDFS4c</th><td>x p y </td><td> x type Ressource<br> y type Ressource</td></tr>");
        MonitoredValues.tips.put("RDFS6", "<tr><th>RDFS6</th><td>x type Property </td><td> x subPropertyOf x</td></tr>");
        MonitoredValues.tips.put("RDFS8", "<tr><th>RDFS8</th><td>x type Class </td><td> x type Ressource</td></tr>");
        MonitoredValues.tips.put("RHODF6a", "<tr><th>RHODF6a</th><td>x p y </td><td> p subPropertyOf p</td></tr>");
        MonitoredValues.tips.put("RHODF6b", "<tr><th>RHODF6b</th><td>p1 subPropertyOf p2 </td><td> p1 subPropertyOf p1<br> p2 subPropertyOf p2</td></tr>");
        MonitoredValues.tips.put("RHODF6d", "<tr><th>RHODF6d</th><td>x p y (p in {domain<br> range} </td><td> x subPropertyOf x</td></tr>");
        MonitoredValues.tips.put("RHODF7a", "<tr><th>RHODF7a</th><td>c1 subClassOf c2 </td><td> c1 subClassOf c1<br> c2 subClassOf c2</td></tr>");
        MonitoredValues.tips.put("RHODF7b", "<tr><th>RHODF7b</th><td>x p y (p in {domain<br> range<br> type} </td><td> x subClassOf x</td></tr>");
        MonitoredValues.tips.put("SCM_DOM1", "<tr><th>SCM_DOM1</th><td>p domain c1<br> c1 subClassOf c2 </td><td> p domain c2</td></tr>");
        MonitoredValues.tips.put("SCM_DOM2", "<tr><th>SCM_DOM2</th><td>p2 domain c<br> p1 subPropertyOf p2 </td><td> p1 domain c</td></tr>");
        MonitoredValues.tips.put("SCM_EQC2", "<tr><th>SCM_EQC2</th><td>c1 subClassOf c2<br> c2 subClassOf c1 </td><td> c1 equivalentClass c2</td></tr>");
        MonitoredValues.tips.put("SCM_EQP2",
                "<tr><th>SCM_EQP2</th><td>c1 subPropertyOf c2<br> c2 subPropertyOf c1 </td><td> c1 equivalentProperty c2</td></tr>");
        MonitoredValues.tips.put("SCM_RNG1", "<tr><th>SCM_RNG1</th><td>p range c1<br> c1 subClassOf c2 </td><td> p range c2</td></tr>");
        MonitoredValues.tips.put("SCM_RNG2", "<tr><th>SCM_RNG2</th><td>p2 range c<br> p1 subPropertyOf p2 </td><td> p1 range c</td></tr>");
        MonitoredValues.tips.put("SCM_SCO", "<tr><th>SCM_SCO</th><td>c1 subClassOf c2<br> c2 subClassOf c3 </td><td> c1 subClassOf c3</td></tr>");
        MonitoredValues.tips.put("SCM_SPO", "<tr><th>SCM_SPO</th><td>p3 subPropertyOf p2<br> p2 subPropertyOf p3 </td><td> p3 subPropertyOf p3</td></tr>");
    }

    public static void initialize(final String fragment, final long bufferSize, final long timeout, final String fileName) {

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                MonitoredValues.saveToJSONObject();
            }
        };

        MonitoredValues.currentInput = new AtomicLong();
        MonitoredValues.currentInfered = new AtomicLong();
        MonitoredValues.runningRules = new AtomicLong();
        MonitoredValues.waitingRules = new AtomicLong();

        MonitoredValues.fragment = fragment;
        MonitoredValues.bufferSize = bufferSize;
        MonitoredValues.timeout = timeout;
        MonitoredValues.fileName = fileName;

        MonitoredValues.buffers = Collections.synchronizedMap(new HashMap<String, AtomicLong>());
        MonitoredValues.runs = Collections.synchronizedMap(new HashMap<String, AtomicLong>());
        MonitoredValues.timeouts = Collections.synchronizedMap(new HashMap<String, AtomicLong>());
        MonitoredValues.inferred = Collections.synchronizedMap(new HashMap<String, AtomicLong>());
        MonitoredValues.lastRun = new ConcurrentLinkedQueue<>();

        MonitoredValues.edges = HashMultimap.create();

        MonitoredValues.tic = Collections.synchronizedCollection(new HashSet<String>());

        MonitoredValues.saveToJSONObject();
    }

    public static void updateLastThings(final long inputTotal, final long inferedTotal) {
        MonitoredValues.totalInput = new AtomicLong(inputTotal);
        MonitoredValues.totalInfered = new AtomicLong(inferedTotal);
    }

    public static void incCurrentInput(final long value) {
        MonitoredValues.currentInput.addAndGet(value);
    }

    public static void incCurrentInfered(final long value) {
        MonitoredValues.currentInfered.addAndGet(value);
    }

    public static void incRunningRules() {
        MonitoredValues.runningRules.incrementAndGet();
    }

    public static void decRunningRules() {
        MonitoredValues.runningRules.decrementAndGet();
    }

    public static void incWaitingRules() {
        MonitoredValues.waitingRules.incrementAndGet();
    }

    public static void decWaitingRules() {
        MonitoredValues.waitingRules.decrementAndGet();
    }

    public static void incRunRule(final String rule) {
        if (MonitoredValues.runs.keySet().contains(rule)) {
            MonitoredValues.runs.get(rule).incrementAndGet();
        } else {
            MonitoredValues.runs.put(rule, new AtomicLong(1));
        }
    }

    public static void incInferredRule(final String rule, final long number) {
        if (MonitoredValues.inferred.keySet().contains(rule)) {
            MonitoredValues.inferred.get(rule).addAndGet(number);
        } else {
            MonitoredValues.inferred.put(rule, new AtomicLong(number));
        }
    }

    public static void updateBuffer(final String rule, final long occupation) {
        if (MonitoredValues.buffers.keySet().contains(rule)) {
            MonitoredValues.buffers.get(rule).set(occupation);
        } else {
            MonitoredValues.buffers.put(rule, new AtomicLong(occupation));
        }
    }

    public static void tic(final String rule) {
        MonitoredValues.tic.add(rule);
        if (MonitoredValues.timeouts.keySet().contains(rule)) {
            MonitoredValues.timeouts.get(rule).incrementAndGet();
        } else {
            MonitoredValues.timeouts.put(rule, new AtomicLong(1));
        }
    }

    public static void addEdge(final String from, final String to) {
        MonitoredValues.edges.put(from, to);
    }

    public static void addRuleRun(final String rule) {
        synchronized (lastRun) {
            if (MonitoredValues.lastRun.size() >= LAST_RUN_LIMIT) {
                MonitoredValues.lastRun.poll();
            }
            MonitoredValues.lastRun.add(rule);
        }
    }

    @SuppressWarnings("unchecked")
    private static void saveToJSONObject() {

        if (currentInput.get() != 0) {
            final JSONObject obj = new JSONObject();

            obj.put("currentInput", MonitoredValues.currentInput.get());
            obj.put("currentInfered", MonitoredValues.currentInfered.get());
            obj.put("runningRules", MonitoredValues.runningRules.get());
            obj.put("waitingRules", MonitoredValues.waitingRules.get());

            final JSONArray list = new JSONArray();
            synchronized (buffers) {
                for (final String key : MonitoredValues.buffers.keySet()) {
                    final JSONObject buffer = new JSONObject();
                    buffer.put("rule", key);
                    buffer.put("occupation", MonitoredValues.buffers.get(key).get());
                    if (MonitoredValues.runs.containsKey(key)) {
                        buffer.put("runs", MonitoredValues.runs.get(key).get());
                    } else {
                        buffer.put("runs", 0);
                    }
                    if (MonitoredValues.inferred.containsKey(key)) {
                        buffer.put("inferred", MonitoredValues.inferred.get(key).get());
                    } else {
                        buffer.put("inferred", 0);
                    }
                    if (MonitoredValues.timeouts.containsKey(key)) {
                        buffer.put("timeouts", MonitoredValues.timeouts.get(key).get());
                    } else {
                        buffer.put("timeouts", 0);
                    }
                    if (MonitoredValues.tic.contains(key)) {
                        buffer.put("tic", 1);
                    }
                    list.add(buffer);
                }
            }

            obj.put("buffers", list);

            final JSONArray lastRuns = new JSONArray();
            lastRuns.addAll(lastRun);
            obj.put("lastRun", lastRuns);

            MonitoredValues.jsons.add(obj);
            MonitoredValues.tic.clear();
        }
    }

    @SuppressWarnings("unchecked")
    public static void persistInFile() {
        final JSONObject obj = new JSONObject();
        obj.put("file", MonitoredValues.fileName.split("\\.nt")[0]);
        obj.put("totalInput", MonitoredValues.totalInput);
        obj.put("totalInfered", MonitoredValues.totalInfered);
        obj.put("fragment", MonitoredValues.fragment.toLowerCase());
        obj.put("bufferSize", MonitoredValues.bufferSize);
        obj.put("timeout", MonitoredValues.timeout);
        obj.put("screenshots", MonitoredValues.jsons);

        final JSONArray runs = new JSONArray();
        synchronized (buffers) {
            for (final String key : MonitoredValues.buffers.keySet()) {
                final JSONObject buffer = new JSONObject();
                buffer.put("rule", key);
                if (MonitoredValues.runs.containsKey(key)) {
                    buffer.put("runs", MonitoredValues.runs.get(key).get());
                } else {
                    buffer.put("runs", 0);
                }
                if (MonitoredValues.inferred.containsKey(key)) {
                    buffer.put("inferred", MonitoredValues.inferred.get(key).get());
                }
                runs.add(buffer);
            }
        }
        obj.put("runs", runs);

        final JSONObject runTips = new JSONObject();
        for (final String key : MonitoredValues.tips.keySet()) {
            if (MonitoredValues.runs.containsKey(key)) {
                runTips.put(key, MonitoredValues.tips.get(key));
            }
        }
        obj.put("tips", runTips);

        final JsonObject dependencyGraph = new JsonObject();
        for (final String from : MonitoredValues.edges.keySet()) {
            final JsonArray edge = new JsonArray();
            for (final String to : MonitoredValues.edges.get(from)) {
                edge.add(to);
            }
            dependencyGraph.put(from, edge);
        }
        obj.put("dependencyGraph", dependencyGraph);

        try {
            final String jsonfile = "jsons/" + MonitoredValues.fileName + "_" + MonitoredValues.fragment.toLowerCase() + "_" + MonitoredValues.bufferSize + "_"
                    + MonitoredValues.timeout + ".json";
            final FileWriter file = new FileWriter(jsonfile);
            file.write(obj.toJSONString());
            file.flush();
            file.close();

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static void start() {
        MonitoredValues.saveToJSONObject();
        MonitoredValues.timer.scheduleAtFixedRate(timerTask, 0, 10);
    }

    public static void stop() {
        MonitoredValues.timer.cancel();
        MonitoredValues.saveToJSONObject();
    }

    public static String print() {
        final StringBuilder sb = new StringBuilder();

        sb.append("input: ");
        sb.append(currentInput);
        sb.append(" infered: ");
        sb.append(currentInfered);
        sb.append(" running: ");
        sb.append(runningRules);
        sb.append(" waiting: ");
        sb.append(waitingRules);
        if (MonitoredValues.buffers.size() > 0) {
            sb.append(" buffers: ");
            for (final String key : MonitoredValues.buffers.keySet()) {
                sb.append(" ");
                sb.append(key);
                sb.append(": ");
                sb.append(MonitoredValues.buffers.get(key));
            }
        }

        return sb.toString();
    }
}
