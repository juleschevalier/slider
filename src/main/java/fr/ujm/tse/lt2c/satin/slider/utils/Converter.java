package fr.ujm.tse.lt2c.satin.slider.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class Converter {

    public static void main(final String[] args) {

        final Model model = ModelFactory.createDefaultModel();
        model.read("/home/jules/Documents/Ontologies/News/mammalian_phenotype.owl");

        OutputStream os;
        try {

            os = new FileOutputStream(new File("/home/jules/Documents/Ontologies/News/mammalian_phenotype.nt"));
            model.write(os, "N-TRIPLES");
            os.close();

        } catch (final FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
