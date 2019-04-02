import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import javax.swing.JOptionPane;

/*
 * reindex.java
 * -------------------
 * Creates and configures empty Elasticsearch indices via HTTP Put requests for each supported similarity model 
 * This operation typically takes only a few seconds as no document data is being transferred
 * 
 * Connects to Elasticsearch reindex API endpoint via HTTP Post to copy data from the initial index to all new indices under the selected similarity algorithms
 * ___________________
 * 
 * Runtime: ~30 minutes (per similarity model selected)
 * 
 * ___________________
 * createIndices()
 * reindexDocuments(String source, String dest)
 */

public class reindex {
	
	
	/*
	 * Create indices for each similarity model
	 * Manually encoded JSON objects in Strings as in this case they are static, one time use
	 */
	public static void createIndices() throws MalformedURLException, IOException {

		HashMap<String, String> algorithms = new HashMap<String, String>();
		//algorithms.put("bm25","..."); bm25 does not need to be included as it is the default similarity model used by Elasticsearch
		algorithms.put("dfi","{\"settings\" : {\"index\" : { \"similarity\" : {\"my_similarity\" : {\"type\" : \"DFI\",\"independence_measure\" : \"standardized\"}}}},\"mappings\" : {\"documents\" : {\"properties\" : {\"content\" : {\"type\" : \"text\",\"similarity\" : \"my_similarity\"}}}}}");
		algorithms.put("dfr","{\"settings\" : {\"index\" : { \"similarity\" : {\"my_similarity\" : {\"type\" : \"DFR\",\"basic_model\" : \"be\",\"after_effect\" : \"no\",\"normalization\" : \"no\"}}}},\"mappings\" : {\"documents\" : {\"properties\" : {\"content\" : {\"type\" : \"text\",\"similarity\" : \"my_similarity\"}}}}}");
		algorithms.put("ib","{\"settings\" : {\"index\" : { \"similarity\" : {\"my_similarity\" : {\"type\" : \"IB\",\"distribution\" : \"ll\",\"lambda\" : \"df\",\"normalization\" : \"no\"}}}},\"mappings\" : {\"documents\" : {\"properties\" : {\"content\" : {\"type\" : \"text\",\"similarity\" : \"my_similarity\"}}}}}");
		algorithms.put("lmdirichlet","{\"settings\" : {\"index\" : { \"similarity\" : {\"my_similarity\" : {\"type\" : \"LMDirichlet\",\"mu\" : \"2000\"}}}},\"mappings\" : {\"documents\" : {\"properties\" : {\"content\" : {\"type\" : \"text\",\"similarity\" : \"my_similarity\"}}}}}");
		algorithms.put("lmjelinekmercer","{\"settings\" : {\"index\" : { \"similarity\" : {\"my_similarity\" : {\"type\" : \"LMJelinekMercer\",\"lambda\" : \"0.1\"}}}},\"mappings\" : {\"documents\" : {\"properties\" : {\"content\" : {\"type\" : \"text\",\"similarity\" : \"my_similarity\"}}}}}");
		algorithms.put("tfidf","{\"settings\" : {\"index\" : { \"similarity\" : {\"my_similarity\" : {\"type\" : \"classic\"}}}},\"mappings\" : {\"documents\" : {\"properties\" : {\"content\" : {\"type\" : \"text\",\"similarity\" : \"my_similarity\"}}}}}");
		
		
		String url = "http://localhost:9200/"; // Elasticsearch host
		
		
		for (String key : algorithms.keySet()) {
		    // ...
			URL put_url = new URL(url + key);
			HttpURLConnection connection = (HttpURLConnection) put_url.openConnection();

			connection.setDoOutput(true); //Triggers POST
			connection.setRequestMethod("PUT");
			connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			connection.setRequestProperty("Accept", "application/json");
			
			System.out.println("Creating index for " + key);
		
			try (OutputStream output = connection.getOutputStream()) {

				System.out.println(algorithms.get(key));
				System.out.println(algorithms.get(key).getBytes("UTF-8"));
			    output.write(algorithms.get(key).getBytes("UTF-8"));
			    output.flush();
			    output.close();
			    System.err.println(connection.getResponseCode());
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Failed to create empty indices\nError: " + e + "\n Is Elasticsearch running? (port 9200)");
				break; // Should not show error message more than once
			}

			
		}
		System.out.println("Finished. Created indexes: " + algorithms.keySet());

		
	}
	
	/*
	 * Create JSON payloads specifying source and destination indices and Post to reindex API
	 */
	public static boolean reindexDocuments(String source, String dest) throws MalformedURLException, IOException {
		String reindex = "{\"source\" : {\"index\" : \"" + source + "\"},\"dest\" : {\"index\" : \"" + dest + "\"}}";
		
		String url = "http://localhost:9200/_reindex"; // Elasticsearch reindex API endpoint
		
		URLConnection connection = new URL(url).openConnection();

		connection.setDoOutput(true); //Triggers POST
		connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		
		System.out.println("Reindexing data with similarity: " + dest);

	
		try (OutputStream output = connection.getOutputStream()) {
		    output.write(reindex.getBytes("UTF-8"));
		}
		catch (Exception e) {
			return false;
		}
		connection.getInputStream().close();

			
		
		System.out.println("Finished. Reindexed data with similarity module " + dest + ".");
		return true;

	}
	
	
}
