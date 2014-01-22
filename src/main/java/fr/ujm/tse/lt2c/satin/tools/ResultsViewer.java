package fr.ujm.tse.lt2c.satin.tools;

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
    public static void main(String[] args) throws UnknownHostException {

        final Configuration config = new Configuration();
        config.setClassForTemplateLoading(ResultsViewer.class, "/");

        MongoClient client = new MongoClient();
        Morphia morphia = new Morphia();
        morphia.map(RunEntity.class);
        final Datastore ds = morphia.createDatastore(client, "RunResults");

        Spark.get(new Route("/") {

            @Override
            public Object handle(Request arg0, Response arg1) {
                StringWriter writer = new StringWriter();
                try {
                    Template hellotemplate = config.getTemplate("run.ftl");
                    Map<String, Object> data = new HashMap<String, Object>();
                    List<String> runs = new ArrayList<String>();
                    for (RunEntity run : ds.find(RunEntity.class).order("-date")) {
                        runs.add(writeRun(run));
                    }
                    data.put("runs", runs);

                    hellotemplate.process(data, writer);
                } catch (Exception e) {
                    logger.error("", e);
                    halt(500);
                }
                return writer;
            }

            private String writeRun(RunEntity run) {

                StringBuilder sb = new StringBuilder();
                String td = "<TD>", etd = "</TD";

                sb.append(td + run.getSessionId() + etd);
                sb.append(td + run.getDate().toLocaleString() + etd);
                sb.append(td + run.getFile() + etd);
                sb.append(td + run.getInferenceTime() + etd);
                sb.append(td + run.getNbInitialTriples() + etd);
                sb.append(td + run.getNbInferedTriples() + etd);
                sb.append(td + run.getLoops() + etd);
                sb.append(td + run.getNbDuplicates() + etd);
                sb.append(td + ((run.getMissingTriples() == null) ? 0 : run.getMissingTriples().size()) + etd);
                sb.append(td + ((run.getTooTriples() == null) ? 0 : run.getTooTriples().size()) + etd);

                return sb.toString();
            }
        });

    }

}
