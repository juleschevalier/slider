package fr.ujm.tse.lt2c.satin.slider.rules.run;

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

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.slider.buffer.BufferTimer;
import fr.ujm.tse.lt2c.satin.slider.buffer.TripleDistributor;
import fr.ujm.tse.lt2c.satin.slider.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.slider.interfaces.TripleBuffer;
import fr.ujm.tse.lt2c.satin.slider.interfaces.TripleStore;

/**
 * @author Jules Chevalier
 *
 */
public final class RunFactory {

    private static final Logger LOGGER = Logger.getLogger(RunFactory.class);

    private RunFactory() {

    }

    public static Thread getRunThread(final Rule run, final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer,
            final TripleDistributor tripleDistributor, final AtomicInteger phaser) {

        return getRunThread(run, dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser, null, 0);
    }

    public static Thread getRunThread(final Rule run, final Dictionary dictionary, final TripleStore tripleStore, final TripleBuffer tripleBuffer,
            final TripleDistributor tripleDistributor, final AtomicInteger phaser, final BufferTimer timer, final long triplesToRead) {

        AbstractRun abstractRun = null;

        switch (run) {
        case CAX_SCO:
            abstractRun = new RunCAX_SCO(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case PRP_DOM:
            abstractRun = new RunPRP_DOM(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case PRP_RNG:
            abstractRun = new RunPRP_RNG(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case PRP_SPO1:
            abstractRun = new RunPRP_SPO1(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case SCM_DOM1:
            abstractRun = new RunSCM_DOM1(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case SCM_DOM2:
            abstractRun = new RunSCM_DOM2(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case SCM_EQC2:
            abstractRun = new RunSCM_EQC2(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case SCM_EQP2:
            abstractRun = new RunSCM_EQP2(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case SCM_RNG1:
            abstractRun = new RunSCM_RNG1(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case SCM_RNG2:
            abstractRun = new RunSCM_RNG2(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case SCM_SCO:
            abstractRun = new RunSCM_SCO(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case SCM_SPO:
            abstractRun = new RunSCM_SPO(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case RDFS4:
            abstractRun = new RunRDFS4(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case RDFS6:
            abstractRun = new RunRDFS6(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case RDFS8:
            abstractRun = new RunRDFS8(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case RDFS10:
            abstractRun = new RunRDFS10(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case RDFS12:
            abstractRun = new RunRDFS12(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case RDFS13:
            abstractRun = new RunRDFS13(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case RHODF6a:
            abstractRun = new RunRHODF6a(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case RHODF6b:
            abstractRun = new RunRHODF6b(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case RHODF6d:
            abstractRun = new RunRHODF6b(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case RHODF7a:
            abstractRun = new RunRHODF6b(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        case RHODF7b:
            abstractRun = new RunRHODF7b(dictionary, tripleStore, tripleBuffer, tripleDistributor, phaser);
            break;
        default:
            LOGGER.error("RUN FACTORY Unknown run type: " + run);
            break;
        }
        if (triplesToRead > 0) {
            abstractRun.setTimerCall(timer, triplesToRead);
        }
        return new Thread(abstractRun);
    }

    public static long[] getInputMatchers(final Rule run) {
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

    public static long[] getOutputMatchers(final Rule run) {
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

    public static String getRuleName(final Rule run) {
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
