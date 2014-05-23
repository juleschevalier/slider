package fr.ujm.tse.lt2c.satin.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;

import fr.ujm.tse.lt2c.satin.dictionary.DictionaryPrimitrivesRWLock;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.reasoner.ReasonerStreamed;
import fr.ujm.tse.lt2c.satin.rules.ReasonerProfile;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;
import fr.ujm.tse.lt2c.satin.utils.GlobalValues;
import fr.ujm.tse.lt2c.satin.utils.ReasoningArguments;
import fr.ujm.tse.lt2c.satin.utils.RunEntity;

/**
 * usage: main
 * -b,--buffer-size <size>......set the buffer size
 * -c,--cumulative..............does not reinit data for each file
 * -d,--directory <directory>...infere on all ontologies in the directory
 * -h,--help....................print this message
 * -i,--iteration <number>......how many times each file
 * -m,--mongo-save..............persists the results in MongoDB
 * -o,--output..................save output into file
 * -p,--profile <profile>.......set the fragment [GRhoDF, RhoDF, RhoDFPP]
 * -t,--threads <number>........set the number of threads by avaible core
 * 
 * @author Jules Chevalier
 */
public class Main {

    private Main() {
    }

    private static final Logger LOGGER = Logger.getLogger(Main.class);

    /* Initialization if default options */
    private static final int DEFAULT_THREADS = 0;
    private static final int DEFAULT_BUFFER_SIZE = 100000;
    private static final boolean DEFAULT_CUMULATIVE_MODE = false;
    private static final boolean DEFAULT_DUMP_MODE = false;
    private static final boolean DEFAULT_PERSIST_MODE = false;
    private static final ReasonerProfile DEFAULT_PROFILE = ReasonerProfile.RHODF;

    public static void main(final String[] args) {

        final ReasoningArguments arguments = getArguments(args);

        if (arguments == null) {
            return;
        }

        TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        Dictionary dictionary = new DictionaryPrimitrivesRWLock();
        ReasonerStreamed reasoner = new ReasonerStreamed(tripleStore, dictionary, arguments);

        if (arguments.getFiles().isEmpty()) {
            LOGGER.warn("No available file.");
            return;
        }

        for (int loop = 0; loop < arguments.getIteration(); loop++) {
            for (final File file : arguments.getFiles()) {

                /*
                 * INFERENCE
                 */
                final RunEntity runEntity = reasoner.infereFromFile(file.getAbsolutePath());

                /* Dumping */
                if (arguments.isDumpMode()) {
                    final File newFile = new File("infered_" + arguments.getProfile() + "_" + file.getName());
                    if (!newFile.exists()) {
                        tripleStore.writeToFile(newFile.getName(), dictionary);
                    }
                }

                /* Reset log tracers */
                GlobalValues.reset();

                if (arguments.isPersistMode() && loop > 0) {
                    try {
                        // TODO Customizable IP
                        final MongoClient client = new MongoClient("10.20.0.57");
                        final Morphia morphia = new Morphia();
                        morphia.map(RunEntity.class);
                        final Datastore ds = morphia.createDatastore(client, "RunResults");
                        ds.save(runEntity);
                    } catch (final Exception e) {
                        LOGGER.error("", e);
                    }
                }

                if (!arguments.isCumulativeMode()) {
                    tripleStore = new VerticalPartioningTripleStoreRWLock();
                    dictionary = new DictionaryPrimitrivesRWLock();
                    reasoner = new ReasonerStreamed(tripleStore, dictionary, arguments);
                }
            }
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("--------AVERAGE TIMES--------");
            for (final String file : GlobalValues.getTimeByFile().keySet()) {
                LOGGER.info(new File(file).getName() + " " + nsToTime(GlobalValues.getTimeByFile().get(file)));
            }
        }

    }

    /**
     * @param args
     * @return a ReasoningArguements object containing all the parsed arguments
     */
    private static ReasoningArguments getArguments(final String[] args) {

        /* Reasoner fields */
        int threads = DEFAULT_THREADS;
        int bufferSize = DEFAULT_BUFFER_SIZE;
        int iteration = 1;
        boolean cumulativeMode = DEFAULT_CUMULATIVE_MODE;
        ReasonerProfile profile = DEFAULT_PROFILE;

        /* Extra fields */
        boolean persistMode = DEFAULT_PERSIST_MODE;
        boolean dumpMode = DEFAULT_DUMP_MODE;

        /*
         * Options
         */

        final Options options = new Options();

        final Option bufferSizeO = new Option("b", "buffer-size", true, "set the buffer size");
        bufferSizeO.setArgName("size");
        bufferSizeO.setArgs(1);
        bufferSizeO.setType(Number.class);
        options.addOption(bufferSizeO);

        final Option iterationO = new Option("i", "iteration", true, "how many times each file ");
        iterationO.setArgName("number");
        iterationO.setArgs(1);
        iterationO.setType(Number.class);
        options.addOption(iterationO);

        options.addOption("c", "cumulative", false, "does not reinit data for each file");

        final Option directoryO = new Option("d", "directory", true, "infere on all ontologies in the directory");
        directoryO.setArgName("directory");
        directoryO.setArgs(1);
        directoryO.setType(File.class);
        options.addOption(directoryO);

        options.addOption("o", "output", false, "save output into file");

        options.addOption("h", "help", false, "print this message");

        options.addOption("m", "mongo-save", false, "persists the results in MongoDB");

        final Option profileO = new Option("p", "profile", true, "set the fragment " + java.util.Arrays.asList(ReasonerProfile.values()));
        profileO.setArgName("profile");
        profileO.setArgs(1);
        options.addOption(profileO);

        final Option threadsO = new Option("t", "threads", true, "set the number of threads by avaible core (0 means the jvm manage)");
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
        /* cumulative */
        if (cmd.hasOption("cumulative")) {
            cumulativeMode = true;
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Cumulative mode enabled");
            }
        }
        /* dump */
        if (cmd.hasOption("output")) {
            dumpMode = true;
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Dump mode enabled");
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
        /* persist */
        if (cmd.hasOption("mongo-save")) {
            persistMode = true;
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Persist mode enabled");
            }
        }
        /* profile */
        if (cmd.hasOption("profile")) {
            final String string = cmd.getOptionValue("profile");
            switch (string) {
            case "RhoDF":
                profile = ReasonerProfile.RHODF;
                break;
            case "GRhoDF":
                profile = ReasonerProfile.GRHODF;
                break;
            case "RhoDFPP":
                profile = ReasonerProfile.RHODFPP;
                break;
            case "RDFS":
                profile = ReasonerProfile.RDFS;
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
                threads = Integer.parseInt(arg);
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
            if (threads > 0) {
                LOGGER.info("Threads:          " + threads);
            } else {
                LOGGER.info("Threads:          Automatique");
            }
            LOGGER.info("Iterations:       " + iteration);
            LOGGER.info("***************************");
        }

        return new ReasoningArguments(threads, bufferSize, iteration, cumulativeMode, profile, persistMode, dumpMode, files);

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
