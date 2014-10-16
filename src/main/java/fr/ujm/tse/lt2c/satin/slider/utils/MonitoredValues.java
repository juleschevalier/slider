package fr.ujm.tse.lt2c.satin.slider.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class MonitoredValues {

    private MonitoredValues() {

    }

    private static AtomicLong totalInput;
    private static AtomicLong currentInput;
    private static AtomicLong totalInfered;
    private static AtomicLong currentInfered;
    private static AtomicLong bufferSize;
    private static AtomicLong runningRules;
    private static AtomicLong waitingRules;
    private static AtomicLong id;

    /* <Rule, triples> */
    private static Map<String, AtomicLong[]> buffers;

    private static long timeout;
    private static String fileName;
    private static Timer timer;
    private static Collection<JSONObject> jsons;
    private static TimerTask timerTask;

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

        MonitoredValues.bufferSize = new AtomicLong();
        MonitoredValues.timeout = 0;
        MonitoredValues.fileName = "";
        MonitoredValues.id = new AtomicLong();

        MonitoredValues.buffers = Collections.synchronizedMap(new HashMap<String, AtomicLong[]>());
    }

    public static void initialize(final long bufferSize, final long timeout, final String fileName) {

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

        MonitoredValues.bufferSize = new AtomicLong(bufferSize);
        MonitoredValues.timeout = timeout;
        MonitoredValues.fileName = fileName;
        MonitoredValues.id = new AtomicLong();

        MonitoredValues.buffers = Collections.synchronizedMap(new HashMap<String, AtomicLong[]>());
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

    public static void updateBuffer(final String rule, final long occupation, final long size) {
        if (MonitoredValues.buffers.keySet().contains(rule)) {
            MonitoredValues.buffers.get(rule)[0].set(occupation);
            MonitoredValues.buffers.get(rule)[1].set(size);
        } else {
            MonitoredValues.buffers.put(rule, new AtomicLong[] { new AtomicLong(occupation), new AtomicLong(size) });
        }
    }

    @SuppressWarnings("unchecked")
    private static void saveToJSONObject() {

        if (currentInput.get() != 0) {
            final JSONObject obj = new JSONObject();

            obj.put("_id", MonitoredValues.id.getAndIncrement());
            obj.put("currentInput", MonitoredValues.currentInput.get());
            obj.put("currentInfered", MonitoredValues.currentInfered.get());
            obj.put("runningRules", MonitoredValues.runningRules.get());
            obj.put("waitingRules", MonitoredValues.waitingRules.get());

            final JSONArray list = new JSONArray();
            synchronized (buffers) {
                for (final String key : MonitoredValues.buffers.keySet()) {
                    final JSONObject buffer = new JSONObject();
                    buffer.put("rule", key);
                    buffer.put("occupation", MonitoredValues.buffers.get(key)[0].get());
                    buffer.put("size", MonitoredValues.buffers.get(key)[1].get());
                    list.add(buffer);
                }
            }

            obj.put("buffers", list);
            MonitoredValues.jsons.add(obj);
            // System.out.println(obj);
        }
    }

    @SuppressWarnings("unchecked")
    public static void persistInFile() {
        final JSONObject obj = new JSONObject();
        obj.put("totalInput", MonitoredValues.totalInput);
        obj.put("totalInfered", MonitoredValues.totalInfered);
        obj.put("bufferSize", MonitoredValues.bufferSize);
        obj.put("timeout", MonitoredValues.timeout);
        obj.put("screenshots", MonitoredValues.jsons);
        final JSONArray screenshots = new JSONArray();
        screenshots.addAll(jsons);
        obj.put("screenshots", screenshots);
        try {
            final FileWriter file = new FileWriter(MonitoredValues.fileName);
            file.write(obj.toJSONString());
            file.flush();
            file.close();

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static void start() {

        MonitoredValues.timer.scheduleAtFixedRate(timerTask, 0, timeout);

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
