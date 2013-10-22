package fr.ujm.tse.lt2c.satin.tools;

import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
					// DBObject document = collection.findOne();
					Map<String, Object> data = new HashMap<String, Object>();
					List<String> runs = new ArrayList<String>();
					for (RunEntity run : ds.find(RunEntity.class).order("-date")) {
						runs.add(writeRun(run));
						// System.out.println(writeRun(run));
					}
					data.put("runs", runs);

					hellotemplate.process(data, writer);
				} catch (Exception e) {
					e.printStackTrace();
					halt(500);
				}
				return writer;
			}

			private String writeRun(RunEntity run) {

				StringBuilder sb = new StringBuilder();

				sb.append("<TD>" + run.getSession_id() + "</TD>");
				sb.append("<TD>" + run.getDate().toLocaleString() + "</TD>");
				sb.append("<TD>" + run.getFile() + "</TD>");
				sb.append("<TD>" + run.getInference_time() + "</TD>");
				sb.append("<TD>" + run.getNb_initial_triples() + "</TD>");
				sb.append("<TD>" + run.getNb_infered_triples() + "</TD>");
				sb.append("<TD>" + run.getLoops() + "</TD>");
				sb.append("<TD>" + run.getNb_duplicates() + "</TD>");
				sb.append("<TD>" + ((run.getMissing_triples() == null) ? 0 : run.getMissing_triples().size()) + "</TD>");
				sb.append("<TD>" + ((run.getToo_triples() == null) ? 0 : run.getToo_triples().size()) + "</TD>");

				return sb.toString();
			}
		});

	}

}
