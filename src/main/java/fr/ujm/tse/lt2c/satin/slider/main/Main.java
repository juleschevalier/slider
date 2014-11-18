package fr.ujm.tse.lt2c.satin.slider.main;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.slider.buffer.BufferTimer;
import fr.ujm.tse.lt2c.satin.slider.buffer.QueuedTripleBufferLock;
import fr.ujm.tse.lt2c.satin.slider.dictionary.DictionaryPrimitrivesRWLock;
import fr.ujm.tse.lt2c.satin.slider.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.slider.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.slider.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.slider.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.slider.reasoner.ReasonerStreamed;
import fr.ujm.tse.lt2c.satin.slider.rules.ReasonerProfile;
import fr.ujm.tse.lt2c.satin.slider.rules.Rule;
import fr.ujm.tse.lt2c.satin.slider.triplestore.VerticalPartioningTripleStoreRWLock;
import fr.ujm.tse.lt2c.satin.slider.utils.GlobalValues;
import fr.ujm.tse.lt2c.satin.slider.utils.MonitoredValues;
import fr.ujm.tse.lt2c.satin.slider.utils.ParserImplNaive;
import fr.ujm.tse.lt2c.satin.slider.utils.ReasoningArguments;
import fr.ujm.tse.lt2c.satin.slider.utils.RunEntity;

/**
 * This class provides a command line interface to use Slider
 * The different options are the following:
 * -b,--buffer-size <time>......set the buffer size
 * -d,--directory <directory>.. infers on all ontologies in the directory
 * -h,--help....................print this message
 * -i,--iteration <number>......how many times each file
 * -n,--threads <number>........set the number of threads by available core (0 means the jvm manage)
 * -o,--output..................save output into file
 * -p,--profile <profile>...... set the fragment [RHODF, BRHODF, RDFS, BRDFS]
 * -r,--batch-reasoning........ enable batch reasoning
 * -t,--timeout <arg>.......... set the buffer timeout in ms
 * -v,--verbose................ enable verbose mode
 * -w,--warm-up................ insert a warm-up lap before the inference
 * 
 * @author Jules Chevalier
 */
public final class Main {

    private Main() {
    }

    private static final Logger LOGGER = Logger.getLogger(Main.class);

    /* Initialization of default options */
    private static final int DEFAULT_THREADS_NB = ReasonerStreamed.DEFAULT_THREADS_NB;
    private static final int DEFAULT_BUFFER_SIZE = QueuedTripleBufferLock.DEFAULT_BUFFER_SIZE;
    private static final long DEFAULT_TIMEOUT = BufferTimer.DEFAULT_TIMEOUT;
    private static final ReasonerProfile DEFAULT_PROFILE = ReasonerStreamed.DEFAULT_PROFILE;
    private static final boolean DEFAULT_DUMP_MODE = false;
    private static final boolean DEFAULT_VERBOSE_MODE = false;
    private static final boolean DEFAULT_WARMUP_MODE = false;
    private static final boolean DEFAULT_BATCH_MODE = false;

    public static void main(final String[] args) throws IOException {
        // System.in.read();
        final ReasoningArguments arguments = getArguments(args);

        if (arguments == null) {
            return;
        }

        if (arguments.getFiles().isEmpty()) {
            LOGGER.warn("No available file.");
            return;
        }

        if (arguments.isWarmupMode()) {
            LOGGER.info("---Warm-up lap---");
            for (final File file : arguments.getFiles()) {
                reason(arguments, file, arguments.isBatchMode());
            }
            LOGGER.info("---Real runs---");
        } else {
            LOGGER.info("---Starting inference---");
        }

        if (arguments.isVerboseMode()) {
            LOGGER.info("File Time Infered Profile Buffer Timeout");
        }
        for (final File file : arguments.getFiles()) {
            for (int i = 0; i < arguments.getIteration(); i++) {
                final File jsonfile = new File("jsons/" + file.getName() + "_" + arguments.getProfile().toString().toLowerCase() + "_"
                        + arguments.getBufferSize() + "_" + arguments.getTimeout() + ".json");
                // if (!jsonfile.exists()) {
                MonitoredValues.initialize(arguments.getProfile().name(), arguments.getBufferSize(), arguments.getTimeout(), file.getName());
                MonitoredValues.start();
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                final RunEntity run = reason(arguments, file, arguments.isBatchMode());
                MonitoredValues.stop();
                MonitoredValues.persistInFile();
                if (arguments.isVerboseMode()) {
                    LOGGER.info(file.getName() + " " + run.getInferenceTime() / 1000000.0 + " " + run.getNbInferedTriples() + " " + run.getProfile() + " "
                            + run.getBufferSize() + " " + run.getTimeout());
                    System.out.println(file.getName() + " " + run.getInferenceTime() / 1000000.0 + " " + run.getNbInferedTriples() + " " + run.getProfile()
                            + " " + run.getBufferSize() + " " + run.getTimeout());
                }
                // } else {
                // System.out.println(file.getName() + " exists");
                // }
            }
        }

        LOGGER.info("---Done---");
    }

    private static RunEntity reason(final ReasoningArguments arguments, final File file, final boolean batchMode) {
        if (batchMode) {
            return reasonBatch(arguments, file);
        }
        return reasonStream(arguments, file);
    }

    private static RunEntity reasonStream(final ReasoningArguments arguments, final File file) {
        final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        final Dictionary dictionary = new DictionaryPrimitrivesRWLock();
        final ReasonerStreamed reasoner = new ReasonerStreamed(tripleStore, dictionary, arguments.getProfile(), arguments.getThreadsNb(),
                arguments.getBufferSize(), arguments.getTimeout());

        final Parser parser = new ParserImplNaive(dictionary, tripleStore);
        final long start = System.nanoTime();
        reasoner.start();

        final int inputSize = parser.parseStream(file.getAbsolutePath(), reasoner);

        reasoner.close();
        try {
            reasoner.join();
        } catch (final InterruptedException e) {
            LOGGER.error("", e);
        }
        final long stop = System.nanoTime();

        MonitoredValues.updateLastThings(inputSize, tripleStore.size() - inputSize);

        if (arguments.isVerboseMode()) {

            final Collection<String> rules = new HashSet<>();
            for (final Rule rule : reasoner.getRules()) {
                rules.add(rule.name());
            }
            final RunEntity run = new RunEntity(arguments.getThreadsNb(), arguments.getBufferSize(), arguments.getTimeout(), "0.9.5", arguments.getProfile()
                    .toString(), rules, UUID.randomUUID().hashCode(), file.getName(), 0, stop - start, inputSize, tripleStore.size() - inputSize,
                    GlobalValues.getRunsByRule(), GlobalValues.getDuplicatesByRule(), GlobalValues.getInferedByRule(), GlobalValues.getTimeoutByRule());
            GlobalValues.reset();
            return run;
        }
        GlobalValues.reset();
        return null;
    }

    private static RunEntity reasonBatch(final ReasoningArguments arguments, final File file) {
        final TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        final Dictionary dictionary = new DictionaryPrimitrivesRWLock();
        final ReasonerStreamed reasoner = new ReasonerStreamed(tripleStore, dictionary, arguments.getProfile(), arguments.getThreadsNb(),
                arguments.getBufferSize(), arguments.getTimeout());

        final Parser parser = new ParserImplNaive(dictionary, tripleStore);

        final long parse = System.nanoTime();
        final Collection<Triple> triples = parser.parse(file.getAbsolutePath());

        final long start = System.nanoTime();
        reasoner.start();

        reasoner.addTriples(triples);

        reasoner.close();
        try {
            reasoner.join();
        } catch (final InterruptedException e) {
            LOGGER.error("", e);
        }
        final long stop = System.nanoTime();

        MonitoredValues.updateLastThings(triples.size(), tripleStore.size() - triples.size());

        if (arguments.isVerboseMode()) {
            final Collection<String> rules = new HashSet<>();
            for (final Rule rule : reasoner.getRules()) {
                rules.add(rule.name());
            }
            final RunEntity run = new RunEntity(arguments.getThreadsNb(), arguments.getBufferSize(), arguments.getTimeout(), "0.9.5", arguments.getProfile()
                    .toString(), rules, UUID.randomUUID().hashCode(), file.getName(), start - parse, stop - start, triples.size(), tripleStore.size()
                    - triples.size(), GlobalValues.getRunsByRule(), GlobalValues.getDuplicatesByRule(), GlobalValues.getInferedByRule(),
                    GlobalValues.getTimeoutByRule());
            GlobalValues.reset();
            return run;
        }
        GlobalValues.reset();
        return null;

    }

    /**
     * @param args
     * @return a ReasoningArguements object containing all the parsed arguments
     */
    private static ReasoningArguments getArguments(final String[] args) {

        /* Reasoner fields */
        int threadsNB = DEFAULT_THREADS_NB;
        int bufferSize = DEFAULT_BUFFER_SIZE;
        long timeout = DEFAULT_TIMEOUT;
        int iteration = 1;
        ReasonerProfile profile = DEFAULT_PROFILE;

        /* Extra fields */
        boolean verboseMode = DEFAULT_VERBOSE_MODE;
        boolean warmupMode = DEFAULT_WARMUP_MODE;
        boolean dumpMode = DEFAULT_DUMP_MODE;
        boolean batchMode = DEFAULT_BATCH_MODE;

        /*
         * Options
         */

        final Options options = new Options();

        final Option bufferSizeO = new Option("b", "buffer-size", true, "set the buffer size");
        bufferSizeO.setArgName("size");
        bufferSizeO.setArgs(1);
        bufferSizeO.setType(Number.class);
        options.addOption(bufferSizeO);

        final Option timeoutO = new Option("t", "timeout", true, "set the buffer timeout in ms (0 means timeout will be disabled)");
        bufferSizeO.setArgName("time");
        bufferSizeO.setArgs(1);
        bufferSizeO.setType(Number.class);
        options.addOption(timeoutO);

        final Option iterationO = new Option("i", "iteration", true, "how many times each file ");
        iterationO.setArgName("number");
        iterationO.setArgs(1);
        iterationO.setType(Number.class);
        options.addOption(iterationO);

        final Option directoryO = new Option("d", "directory", true, "infers on all ontologies in the directory");
        directoryO.setArgName("directory");
        directoryO.setArgs(1);
        directoryO.setType(File.class);
        options.addOption(directoryO);

        options.addOption("o", "output", false, "save output into file");

        options.addOption("h", "help", false, "print this message");

        options.addOption("v", "verbose", false, "enable verbose mode");

        options.addOption("r", "batch-reasoning", false, "enable batch reasoning");

        options.addOption("w", "warm-up", false, "insert a warm-up lap before the inference");

        final Option profileO = new Option("p", "profile", true, "set the fragment " + java.util.Arrays.asList(ReasonerProfile.values()));
        profileO.setArgName("profile");
        profileO.setArgs(1);
        options.addOption(profileO);

        final Option threadsO = new Option("n", "threads", true, "set the number of threads by available core (0 means the jvm manage)");
        threadsO.setArgName("number");
        threadsO.setArgs(1);
        threadsO.setType(Number.class);
        options.addOption(threadsO);

        /*
         * Arguments parsing
         */
        final CommandLineParser parser = new GnuParser();
        final CommandLine cmd;
        try {
            cmd = parser.parse(options, args);

        } catch (final ParseException e) {
            LOGGER.error("", e);
            return null;
        }

        /* help */
        if (cmd.hasOption("help")) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("main", options);
            return null;
        }

        /* buffer */
        if (cmd.hasOption("buffer-size")) {
            final String arg = cmd.getOptionValue("buffer-size");
            try {
                bufferSize = Integer.parseInt(arg);
            } catch (final NumberFormatException e) {
                LOGGER.error("Buffer size must be a number. Default value used", e);
            }
        }
        /* timeout */
        if (cmd.hasOption("timeout")) {
            final String arg = cmd.getOptionValue("timeout");
            try {
                timeout = Integer.parseInt(arg);
                if (timeout <= 0) {
                    throw new RuntimeException("Timeout must be >0");
                }
            } catch (final NumberFormatException e) {
                LOGGER.error("Timeout must be a number. Default value used", e);
            }
        }
        /* verbose */
        if (cmd.hasOption("verbose")) {
            verboseMode = true;
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Verbose mode enabled");
            }
        }
        /* warm-up */
        if (cmd.hasOption("warm-up")) {
            warmupMode = true;
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Warm-up mode enabled");
            }
        }
        /* dump */
        if (cmd.hasOption("output")) {
            dumpMode = true;
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Dump mode enabled");
            }
        }
        /* dump */
        if (cmd.hasOption("batch-reasoning")) {
            batchMode = true;
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Batch mode enabled");
            }
        }
        /* directory */
        String dir = null;
        if (cmd.hasOption("directory")) {
            for (final Object o : cmd.getOptionValues("directory")) {
                String arg = o.toString();
                if (arg.startsWith("~" + File.separator)) {
                    arg = System.getProperty("user.home") + arg.substring(1);
                }
                final File directory = new File(arg);
                if (!directory.exists()) {
                    LOGGER.warn("**Cant not find " + directory);
                } else if (!directory.isDirectory()) {
                    LOGGER.warn("**" + directory + " is not a directory");
                } else {
                    dir = directory.getAbsolutePath();
                }
            }
        }
        /* profile */
        if (cmd.hasOption("profile")) {
            final String string = cmd.getOptionValue("profile");
            switch (string.toLowerCase()) {
            case "rhodf":
                profile = ReasonerProfile.RHODF;
                break;
            case "brhodf":
                profile = ReasonerProfile.BRHODF;
                break;
            case "rdfs":
                profile = ReasonerProfile.RDFS;
                break;
            case "brdfs":
                profile = ReasonerProfile.BRDFS;
                break;

            default:
                LOGGER.warn("Profile unknown, default profile used: " + DEFAULT_PROFILE);
                profile = DEFAULT_PROFILE;
                break;
            }
        }
        /* threads */
        if (cmd.hasOption("threads")) {
            final String arg = cmd.getOptionValue("threads");
            try {
                threadsNB = Integer.parseInt(arg);
            } catch (final NumberFormatException e) {
                LOGGER.error("Threads number must be a number. Default value used", e);
            }
        }
        /* iteration */
        if (cmd.hasOption("iteration")) {
            final String arg = cmd.getOptionValue("iteration");
            try {
                iteration = Integer.parseInt(arg);
            } catch (final NumberFormatException e) {
                LOGGER.error("Iteration must be a number. Default value used", e);
            }
        }

        final List<File> files = new ArrayList<>();
        if (dir != null) {
            final File directory = new File(dir);
            final File[] listOfFiles = directory.listFiles();

            for (final File file : listOfFiles) {
                // Maybe other extensions ?
                if (file.isFile() && file.getName().endsWith(".nt")) {
                    files.add(file);
                }
            }
        }
        for (final Object o : cmd.getArgList()) {
            String arg = o.toString();
            if (arg.startsWith("~" + File.separator)) {
                arg = System.getProperty("user.home") + arg.substring(1);
            }
            final File file = new File(arg);
            if (!file.exists()) {
                LOGGER.warn("**Cant not find " + file);
            } else if (file.isDirectory()) {
                LOGGER.warn("**" + file + " is a directory");
            } else {
                files.add(file);
            }
        }

        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(final File f1, final File f2) {
                if (f1.length() > f2.length()) {
                    return 1;
                }
                if (f2.length() > f1.length()) {
                    return -1;
                }
                return 0;
            }
        });

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("********* OPTIONS *********");
            LOGGER.info("Buffer size:      " + bufferSize);
            LOGGER.info("Profile:          " + profile);
            if (threadsNB > 0) {
                LOGGER.info("Threads:          " + threadsNB);
            } else {
                LOGGER.info("Threads:          Automatic");
            }
            LOGGER.info("Iterations:       " + iteration);
            LOGGER.info("Timeout:          " + timeout);
            LOGGER.info("***************************");
        }

        return new ReasoningArguments(threadsNB, bufferSize, timeout, iteration, profile, verboseMode, warmupMode, dumpMode, batchMode, files);

    }

    /**
     * @param bytes
     * @return the size in human readable format
     */
    public static String humanReadableSize(final long bytes) {
        final int unit = 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        final int exp = (int) (Math.log(bytes) / Math.log(unit));
        final String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%6.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * @param timeInNs
     * @return the time in format HH:MM:SS.ms
     */
    public static String nsToTime(final long timeInNs) {

        long left = timeInNs;

        final long hours = left / new Long("3600000000000");
        left -= hours * new Long("3600000000000");

        final long minutes = left / new Long("60000000000");
        left -= minutes * new Long("60000000000");

        final long secondes = left / new Long("1000000000");
        left -= secondes * new Long("1000000000");

        final long msecondes = left / new Long("1000000");
        left -= msecondes * new Long("1000000");
        final StringBuilder result = new StringBuilder();

        if (hours > 0) {
            result.append(hours + ":");
        }
        if (minutes > 0) {
            result.append(minutes + ":");
        }
        result.append(secondes + ".");
        result.append(msecondes);

        return result.toString();
    }
}
