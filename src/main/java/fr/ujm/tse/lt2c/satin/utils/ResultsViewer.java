package fr.ujm.tse.lt2c.satin.utils;

import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import com.mongodb.MongoClient;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class ResultsViewer {

    private ResultsViewer() {
    }

    private static Logger logger = Logger.getLogger(ResultsViewer.class);

    /**
     * @param args
     * @throws UnknownHostException
     */
    public static void main(final String[] args) throws UnknownHostException {

        final Configuration config = new Configuration();
        config.setClassForTemplateLoading(ResultsViewer.class, "/");

        final MongoClient client = new MongoClient();
        final Morphia morphia = new Morphia();
        morphia.map(RunEntity.class);
        final Datastore ds = morphia.createDatastore(client, "RunResults");

        Spark.get(new Route("/") {

            @Override
            public Object handle(final Request arg0, final Response arg1) {
                final StringWriter writer = new StringWriter();
                try {
                    final Template hellotemplate = config.getTemplate("run.ftl");
                    final Map<String, Object> data = new HashMap<String, Object>();
                    final List<String> runs = new ArrayList<String>();
                    for (final RunEntity run : ds.find(RunEntity.class).order("-date")) {
                        runs.add(this.writeRun(run));
                    }
                    data.put("runs", runs);

                    hellotemplate.process(data, writer);
                } catch (final Exception e) {
                    logger.error("", e);
                    this.halt(500);
                }
                return writer;
            }

            private String writeRun(final RunEntity run) {

                final StringBuilder sb = new StringBuilder();
                final String td = "<TD>", etd = "</TD";

                sb.append(td + run.getSessionId() + etd);
                sb.append(td + run.getDate().toLocaleString() + etd);
                sb.append(td + run.getFileInput() + etd);
                sb.append(td + run.getInferenceTime() + etd);
                sb.append(td + run.getNbInitialTriples() + etd);
                sb.append(td + run.getNbInferedTriples() + etd);
                sb.append(td + run.getRunsByRule().size() + etd);
                sb.append(td + run.getDuplicatesByRule().size() + etd);
                sb.append(td + run.getThreadsNb() + etd);
                sb.append(td + run.getVersion() + etd);

                return sb.toString();
            }
        });

    }

}
