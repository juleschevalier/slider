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

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
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

        // final MongoClient client = new MongoClient("10.20.0.57");
        final MongoClient client = new MongoClient();
        final Morphia morphia = new Morphia();
        morphia.map(RunEntity.class);
        final Datastore ds = morphia.createDatastore(client, "RunResults");

        Spark.get(new Route("/") {

            @Override
            public Object handle(final Request arg0, final Response arg1) {
                final StringWriter writer = new StringWriter();
                try {

                    final DBCollection collection = ds.getCollection(RunEntity.class);
                    final DBObject ids = new BasicDBObject("file", "$fileInput");
                    ids.put("input", "$nbInitialTriples");
                    ids.put("output", "$nbInferedTriples");
                    ids.put("threads", "$threadsNb");
                    ids.put("buffer", "$bufferSize");
                    final DBObject groupFields = new BasicDBObject("_id", ids);
                    groupFields.put("avgTime", new BasicDBObject("$avg", "$inferenceTime"));
                    groupFields.put("avgParsing", new BasicDBObject("$avg", "$parsingTime"));

                    final DBObject group = new BasicDBObject("$group", groupFields);

                    final AggregationOutput output = collection.aggregate(group);

                    System.out.println(output.getCommandResult());

                    final Template hellotemplate = config.getTemplate("run.ftl");
                    final Map<String, Object> data = new HashMap<String, Object>();
                    final List<String> runs = new ArrayList<String>();
                    for (final DBObject object : output.results()) {
                        runs.add(this.writeRun(object));
                    }
                    data.put("runs", runs);

                    hellotemplate.process(data, writer);
                } catch (final Exception e) {
                    logger.error("", e);
                    System.exit(-1);
                    this.halt(500);
                }
                return writer;
            }

            private String writeRun(final DBObject object) {
                if ((double) object.get("avgTime") == 0) {
                    return "";
                }
                // <TH>File</TH>
                // <TH>Input</TH>
                // <TH>Inferred</TH>
                // <TH>Threads</TH>
                // <TH>Buffer</TH>
                // <TH>Inference Time</TH>
                // <TH>Parsing Time</TH>

                final StringBuilder sb = new StringBuilder();
                final String td = "<TD>", etd = "</TD>";

                final String[] file = ((DBObject) object.get("_id")).get("file").toString().split("/");
                sb.append(td + file[file.length - 1] + etd);
                sb.append(td + ((DBObject) object.get("_id")).get("input") + etd);
                sb.append(td + ((DBObject) object.get("_id")).get("output") + etd);
                sb.append(td + ((DBObject) object.get("_id")).get("threads") + etd);
                sb.append(td + ((DBObject) object.get("_id")).get("buffer") + etd);
                final String avgTime = this.nsToTime((new Double(object.get("avgTime").toString()).longValue()));
                sb.append(td + avgTime + etd);
                final String avgParsing = this.nsToTime((new Double(object.get("avgParsing").toString()).longValue()));
                sb.append(td + avgParsing + etd);

                return sb.toString();
            }

            private String nsToTime(final long timeInNs) {

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
                if (msecondes > 0) {
                    result.append(msecondes);
                }

                return result.toString();
            }
        });

    }
}
