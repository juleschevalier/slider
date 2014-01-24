package fr.ujm.tse.lt2c.satin.rules.run;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;

public class RunFactory {

    private static final Logger logger = Logger.getLogger(RunFactory.class);

    private RunFactory() {

    }

    public static AbstractRun getRunInstance(AvaibleRuns run, Dictionary dictionary, TripleStore tripleStore, TripleBuffer tripleBuffer, TripleDistributor tripleDistributor, AtomicInteger phaser) {

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
        default:
            logger.error("RUN FACTORY Unknown run type: " + run);
            break;
        }
        return null;
    }

    public static long[] getInputMatchers(AvaibleRuns run) {
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
        default:
            logger.error("RUN FACTORY Unknown run type: " + run);
            break;
        }
        return null;
    }

    public static long[] getOutputMatchers(AvaibleRuns run) {
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

        default:
            logger.error("RUN FACTORY Unknown run type: " + run);
            break;
        }
        return null;

    }

    public static String getRuleName(AvaibleRuns run) {
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

        default:
            logger.error("RUN FACTORY Unknown run type: " + run);
            break;
        }
        return null;
    }
}
