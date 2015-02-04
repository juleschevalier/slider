package fr.ujm.tse.lt2c.satin.slider.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import fr.ujm.tse.lt2c.satin.slider.triplestore.VerticalPartioningTripleStore;

public class Converter {
    private static final Logger LOGGER = Logger.getLogger(VerticalPartioningTripleStore.class);

    private Converter() {
        super();
    }

    public static void main(final String[] args) {

        final Model model = ModelFactory.createDefaultModel();
        model.read("/home/jules/Documents/Ontologies/News/mammalian_phenotype.owl");

        OutputStream os;
        try {

            os = new FileOutputStream(new File("/home/jules/Documents/Ontologies/News/mammalian_phenotype.nt"));
            model.write(os, "N-TRIPLES");
            os.close();

        } catch (final FileNotFoundException e) {
            LOGGER.error("", e);
        } catch (final IOException e) {
            LOGGER.error("", e);
        }
    }

}
