package fr.ujm.tse.lt2c.satin.rules.run;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.buffer.BufferTimer;
import fr.ujm.tse.lt2c.satin.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;

public class RunFactory {

    private static final Logger LOGGER = Logger.getLogger(RunFactory.class);

    private RunFactory() {

    }

    public static AbstractRun getRunInstance(final AvaibleRuns run, final Dictionary dictionary, final TripleStore tripleStore,
            final TripleBuffer tripleBuffer, final TripleDistributor tripleDistributor, final AtomicInteger phaser, final BufferTimer timer) {

        switch (run) {
        case CAX_SCO:
            return new RunCAX_SCO(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case PRP_DOM:
            return new RunPRP_DOM(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case PRP_RNG:
            return new RunPRP_RNG(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case PRP_SPO1:
            return new RunPRP_SPO1(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case SCM_DOM1:
            return new RunSCM_DOM1(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case SCM_DOM2:
            return new RunSCM_DOM2(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case SCM_EQC2:
            return new RunSCM_EQC2(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case SCM_EQP2:
            return new RunSCM_EQP2(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case SCM_RNG1:
            return new RunSCM_RNG1(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case SCM_RNG2:
            return new RunSCM_RNG2(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case SCM_SCO:
            return new RunSCM_SCO(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case SCM_SPO:
            return new RunSCM_SPO(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case RDFS4:
            return new RunRDFS4(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case RDFS6:
            return new RunRDFS6(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case RDFS8:
            return new RunRDFS8(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case RDFS10:
            return new RunRDFS10(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case RDFS12:
            return new RunRDFS12(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case RDFS13:
            return new RunRDFS13(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case RHODF6a:
            return new RunRHODF6a(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case RHODF6b:
            return new RunRHODF6b(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case RHODF6d:
            return new RunRHODF6b(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case RHODF7a:
            return new RunRHODF6b(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        case RHODF7b:
            return new RunRHODF7b(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
        default:
            LOGGER.error("RUN FACTORY Unknown run type: " + run);
            break;
        }
        return null;
    }

    public static long[] getInputMatchers(final AvaibleRuns run) {
        switch (run) {
        case CAX_SCO:
            return RunCAX_SCO.INPUT_MATCHERS;
        case PRP_DOM:
            return RunPRP_DOM.INPUT_MATCHERS;
        case PRP_RNG:
            return RunPRP_RNG.INPUT_MATCHERS;
        case PRP_SPO1:
            return RunPRP_SPO1.INPUT_MATCHERS;
        case SCM_DOM1:
            return RunSCM_DOM1.INPUT_MATCHERS;
        case SCM_DOM2:
            return RunSCM_DOM2.INPUT_MATCHERS;
        case SCM_EQC2:
            return RunSCM_EQC2.INPUT_MATCHERS;
        case SCM_EQP2:
            return RunSCM_EQP2.INPUT_MATCHERS;
        case SCM_RNG1:
            return RunSCM_RNG1.INPUT_MATCHERS;
        case SCM_RNG2:
            return RunSCM_RNG2.INPUT_MATCHERS;
        case SCM_SCO:
            return RunSCM_SCO.INPUT_MATCHERS;
        case SCM_SPO:
            return RunSCM_SPO.INPUT_MATCHERS;
        case RDFS4:
            return RunRDFS4.INPUT_MATCHERS;
        case RDFS6:
            return RunRDFS6.INPUT_MATCHERS;
        case RDFS8:
            return RunRDFS8.INPUT_MATCHERS;
        case RDFS10:
            return RunRDFS10.INPUT_MATCHERS;
        case RDFS12:
            return RunRDFS12.INPUT_MATCHERS;
        case RDFS13:
            return RunRDFS13.INPUT_MATCHERS;
        case RHODF6a:
            return RunRHODF6a.INPUT_MATCHERS;
        case RHODF6b:
            return RunRHODF6b.INPUT_MATCHERS;
        case RHODF6d:
            return RunRHODF6d.INPUT_MATCHERS;
        case RHODF7a:
            return RunRHODF7a.INPUT_MATCHERS;
        case RHODF7b:
            return RunRHODF7b.INPUT_MATCHERS;
        default:
            LOGGER.error("RUN FACTORY Unknown run type: " + run);
            break;
        }
        return new long[0];
    }

    public static long[] getOutputMatchers(final AvaibleRuns run) {
        switch (run) {
        case CAX_SCO:
            return RunCAX_SCO.OUTPUT_MATCHERS;
        case PRP_DOM:
            return RunPRP_DOM.OUTPUT_MATCHERS;
        case PRP_RNG:
            return RunPRP_RNG.OUTPUT_MATCHERS;
        case PRP_SPO1:
            return RunPRP_SPO1.OUTPUT_MATCHERS;
        case SCM_DOM1:
            return RunSCM_DOM1.OUTPUT_MATCHERS;
        case SCM_DOM2:
            return RunSCM_DOM2.OUTPUT_MATCHERS;
        case SCM_EQC2:
            return RunSCM_EQC2.OUTPUT_MATCHERS;
        case SCM_EQP2:
            return RunSCM_EQP2.OUTPUT_MATCHERS;
        case SCM_RNG1:
            return RunSCM_RNG1.OUTPUT_MATCHERS;
        case SCM_RNG2:
            return RunSCM_RNG2.OUTPUT_MATCHERS;
        case SCM_SCO:
            return RunSCM_SCO.OUTPUT_MATCHERS;
        case SCM_SPO:
            return RunSCM_SPO.OUTPUT_MATCHERS;
        case RDFS4:
            return RunRDFS4.OUTPUT_MATCHERS;
        case RDFS6:
            return RunRDFS6.OUTPUT_MATCHERS;
        case RDFS8:
            return RunRDFS8.OUTPUT_MATCHERS;
        case RDFS10:
            return RunRDFS10.OUTPUT_MATCHERS;
        case RDFS12:
            return RunRDFS12.OUTPUT_MATCHERS;
        case RDFS13:
            return RunRDFS13.OUTPUT_MATCHERS;
        case RHODF6a:
            return RunRHODF6a.OUTPUT_MATCHERS;
        case RHODF6b:
            return RunRHODF6b.OUTPUT_MATCHERS;
        case RHODF6d:
            return RunRHODF6d.OUTPUT_MATCHERS;
        case RHODF7a:
            return RunRHODF7a.OUTPUT_MATCHERS;
        case RHODF7b:
            return RunRHODF7b.OUTPUT_MATCHERS;
        default:
            LOGGER.error("RUN FACTORY Unknown run type: " + run);
            break;
        }
        return new long[0];

    }

    public static String getRuleName(final AvaibleRuns run) {
        switch (run) {
        case CAX_SCO:
            return "CAX_SCO";
        case PRP_DOM:
            return "PRP_DOM";
        case PRP_RNG:
            return "PRP_RNG";
        case PRP_SPO1:
            return "PRP_SPO1";
        case SCM_DOM1:
            return "SCM_DOM1";
        case SCM_DOM2:
            return "SCM_DOM2";
        case SCM_EQC2:
            return "SCM_EQC2";
        case SCM_EQP2:
            return "SCM_EQP2";
        case SCM_RNG1:
            return "SCM_RNG1";
        case SCM_RNG2:
            return "SCM_RNG2";
        case SCM_SCO:
            return "SCM_SCO";
        case SCM_SPO:
            return "SCM_SPO";
        case RDFS4:
            return "RDFS4";
        case RDFS6:
            return "RDFS6";
        case RDFS8:
            return "RDFS8";
        case RDFS10:
            return "RDFS10";
        case RDFS12:
            return "RDFS12";
        case RDFS13:
            return "RDFS13";
        case RHODF6a:
            return "RHODF6a";
        case RHODF6b:
            return "RHODF6b";
        case RHODF6d:
            return "RHODF6d";
        case RHODF7a:
            return "RHODF7a";
        case RHODF7b:
            return "RHODF7b";

        default:
            LOGGER.error("RUN FACTORY Unknown run type: " + run);
            break;
        }
        return null;
    }
}
