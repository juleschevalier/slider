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
import fr.ujm.tse.lt2c.satin.rules.run.ReasonerProfile;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;
import fr.ujm.tse.lt2c.satin.utils.GlobalValues;
import fr.ujm.tse.lt2c.satin.utils.ReasoningArguments;
import fr.ujm.tse.lt2c.satin.utils.RunEntity;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);

    private final static int DEFAULT_THREADS_PER_CORE = 10;
    private final static int DEFAULT_BUFFER_SIZE = 100;
    private final static long DEFAULT_TIMEOUT = 100;
    private final static boolean DEFAULT_CUMULATIVE_MODE = false;
    private final static boolean DEFAULT_DUMP_MODE = false;
    private final static boolean DEFAULT_PERSIST_MODE = false;
    private final static ReasonerProfile DEFAULT_PROFILE = ReasonerProfile.RhoDF;

    public static void main(final String[] args) {

        final ReasoningArguments arguments = getArguments(args);

        if (arguments == null) {
            return;
        }

        TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
        Dictionary dictionary = new DictionaryPrimitrivesRWLock();
        ReasonerStreamed reasoner = new ReasonerStreamed(tripleStore, dictionary, arguments);

        if (logger.isInfoEnabled()) {
            logger.info("Files:");
            int maxLenght = 0;
            for (final File file : arguments.getFiles()) {
                if (file.getName().length() > maxLenght) {
                    maxLenght = file.getName().length();
                }
            }
            for (final File file : arguments.getFiles()) {
                final StringBuilder sb = new StringBuilder();
                sb.append("       ");
                sb.append(file.getName());
                for (int i = file.getName().length(); i < (maxLenght + 2); i++) {
                    sb.append(" ");
                }
                sb.append(humanReadableSize(file.length()));
                logger.info(sb.toString());
            }
        }

        if (arguments.getFiles().isEmpty()) {
            logger.warn("Well, without files I can't do anything, you know ?");
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("******** LET'S GO ********");
        }

        long infered = 0;

        for (int loop = 0; loop < arguments.getIteration(); loop++) {
            if ((arguments.getIteration() > 1) && logger.isInfoEnabled()) {
                logger.info("\tIteration: " + loop);
            }
            for (final File file : arguments.getFiles()) {

                final RunEntity runEntity = reasoner.infereFromFile(file.getAbsolutePath());

                if (arguments.isDumpMode()) {
                    final File newFile = new File("infered_" + arguments.getProfile() + "_" + runEntity.getNbInferedTriples() + "_" + file.getName());
                    if (!newFile.exists()) {
                        tripleStore.writeToFile(newFile.getName(), dictionary);
                        if (logger.isInfoEnabled()) {
                            logger.info("Dumped in " + newFile);
                        }
                    }
                }

                if (runEntity.getNbInferedTriples() != infered) {
                    infered = runEntity.getNbInferedTriples();
                    System.out.println(infered);
                }

                /* Reset log tracers */
                GlobalValues.reset();

                if (arguments.isPersistMode()) {
                    final MongoClient client;
                    try {
                        client = new MongoClient("10.20.0.57");
                        // client = new MongoClient();
                        final Morphia morphia = new Morphia();
                        morphia.map(RunEntity.class);
                        final Datastore ds = morphia.createDatastore(client, "RunResults");
                        ds.save(runEntity);
                    } catch (final Exception e) {
                        logger.error("", e);
                    }
                }

                if (!arguments.isCumulativeMode()) {
                    tripleStore = new VerticalPartioningTripleStoreRWLock();
                    dictionary = new DictionaryPrimitrivesRWLock();
                    reasoner = new ReasonerStreamed(tripleStore, dictionary, arguments);
                }
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("--------AVERAGE TIMES--------");
            for (final String file : GlobalValues.getTimeByFile().keySet()) {
                logger.info(file + " " + nsToTime(GlobalValues.getTimeByFile().get(file)));
            }
        }
        System.exit(-1);

    }

    private static ReasoningArguments getArguments(final String[] args) {

        /* Reasoner fields */
        int threadsPerCore = DEFAULT_THREADS_PER_CORE;
        int bufferSize = DEFAULT_BUFFER_SIZE;
        long timeout = DEFAULT_TIMEOUT;
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

        // options.addOption("buffer", true, "set the buffer size");
        final Option bufferSizeO = new Option("buffer", "set the buffer size");
        bufferSizeO.setArgName("size");
        bufferSizeO.setArgs(1);
        bufferSizeO.setType(Number.class);
        options.addOption(bufferSizeO);

        final Option iterationO = new Option("iteration", "how many times each file ");
        iterationO.setArgName("number");
        iterationO.setArgs(1);
        iterationO.setType(Number.class);
        options.addOption(iterationO);

        options.addOption("cumulative", false, "does not reinit data for each file");

        // options.addOption("directory", false,
        // "parameters are directories instead of files. Inferes on all files in it");
        final Option directoryO = new Option("d", "infere on all ontologies in the indicated directory");
        directoryO.setArgName("directory");
        directoryO.setArgs(1);
        directoryO.setType(File.class);
        options.addOption(directoryO);

        options.addOption("dump", false, "enable dump of inferred triples");

        options.addOption("help", false, "print this message");

        options.addOption("persist", false, "persists the results in MongoDB");

        // options.addOption("profile", true,
        // "set the fragment (RDFS, RhoDF, ...)");
        final Option profileO = new Option("profile", "set the fragment " + ReasonerProfile.values());
        profileO.setArgName("profile");
        profileO.setArgs(1);
        options.addOption(profileO);

        // options.addOption("threads", true,
        // "set the number of threads by avaible cores");
        final Option threadsO = new Option("threads", "set the number of threads by avaible cores");
        threadsO.setArgName("number");
        threadsO.setArgs(1);
        threadsO.setType(Number.class);
        options.addOption(threadsO);

        // options.addOption("timeout", true,
        // "enable and set timeout before a non-full buffer is flushed, in ms");
        final Option timeoutO = new Option("timeout", "enable and set timeout before a non-full buffer is flushed, in ms");
        timeoutO.setArgName("time");
        timeoutO.setArgs(1);
        timeoutO.setType(Number.class);
        options.addOption(timeoutO);

        /*
         * Arguments parsing
         */
        final CommandLineParser parser = new GnuParser();
        try {
            final CommandLine cmd = parser.parse(options, args);

            /* help */
            if (cmd.hasOption("help")) {
                final HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("main", options);
                return null;
            }

            if (logger.isInfoEnabled()) {
                logger.info("******** OPTIONS ********");
            }

            /* buffer */
            if (cmd.hasOption("buffer")) {
                final String arg = cmd.getOptionValue("buffer");
                try {
                    bufferSize = Integer.parseInt(arg);
                    if (logger.isInfoEnabled()) {
                        logger.info("buffer: " + bufferSize);
                    }
                } catch (final NumberFormatException e) {
                    logger.error("Buffer size must be a number. Option ignored");
                }
            }
            /* cumulative */
            if (cmd.hasOption("cumulative")) {
                cumulativeMode = true;
                if (logger.isInfoEnabled()) {
                    logger.info("Cumulative mode enabled");
                }
            }
            /* dump */
            if (cmd.hasOption("dump")) {
                dumpMode = true;
                if (logger.isInfoEnabled()) {
                    logger.info("Dump mode enabled");
                }
            }
            /* directory */
            String dir = null;
            if (cmd.hasOption("d")) {
                for (final Object o : cmd.getOptionValues("d")) {
                    String arg = o.toString();
                    if (arg.startsWith("~" + File.separator)) {
                        arg = System.getProperty("user.home") + arg.substring(1);
                    }
                    final File directory = new File(arg);
                    if (!directory.exists()) {
                        logger.warn("**Cant not find " + directory);
                    } else if (!directory.isDirectory()) {
                        logger.warn("**" + directory + " is not a directory");
                    } else {
                        dir = directory.getAbsolutePath();
                    }
                }
            }
            /* persist */
            if (cmd.hasOption("persist")) {
                persistMode = true;
                if (logger.isInfoEnabled()) {
                    logger.info("Persist mode enabled");
                }
            }
            /* profile */
            if (cmd.hasOption("profile")) {
                final String string = cmd.getOptionValue("profile");
                switch (string) {
                case "RhoDF":
                    profile = ReasonerProfile.RhoDF;
                    break;
                case "GRhoDF":
                    profile = ReasonerProfile.GRhoDF;
                    break;

                default:
                    profile = DEFAULT_PROFILE;
                    break;
                }
                if (logger.isInfoEnabled()) {
                    logger.info("Profile: " + profile);
                }
            }
            /* threads */
            if (cmd.hasOption("threads")) {
                final String arg = cmd.getOptionValue("threads");
                try {
                    threadsPerCore = Integer.parseInt(arg);
                    if (logger.isInfoEnabled()) {
                        logger.info("Threads: " + iteration);
                    }
                } catch (final NumberFormatException e) {
                    logger.error("Threads must be a number. Option ignored");
                }
            }
            /* timeout */
            if (cmd.hasOption("timeout")) {
                final String arg = cmd.getOptionValue("timeout");
                try {
                    timeout = Integer.parseInt(arg);
                } catch (final NumberFormatException e) {
                    logger.error("Timeout must be a number. Option ignored");
                }
            }
            /* iteration */
            if (cmd.hasOption("iteration")) {
                final String arg = cmd.getOptionValue("iteration");
                try {
                    iteration = Integer.parseInt(arg);
                    if (logger.isInfoEnabled()) {
                        logger.info("Iterations: " + iteration);
                    }
                } catch (final NumberFormatException e) {
                    logger.error("Iteration must be a number. Option ignored");
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
                    logger.warn("**Cant not find " + file);
                } else if (file.isDirectory()) {
                    logger.warn("**" + file + " is a directory");
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

            return new ReasoningArguments(threadsPerCore, bufferSize, timeout, iteration, cumulativeMode, profile, persistMode, dumpMode, files);

        } catch (final ParseException e) {
            logger.error("", e);
        }
        return null;
    }

    public static String humanReadableSize(final long bytes) {
        final int unit = 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        final int exp = (int) (Math.log(bytes) / Math.log(unit));
        final String pre = ("KMGTPE").charAt(exp - 1) + "";
        return String.format("%6.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

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
