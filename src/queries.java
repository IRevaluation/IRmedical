import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.json.JSONObject;


/*
 * queries.java
 * -------------------
 * Read all queries from query file
 * Parse queries to ensure no problem characters
 * Build JSON payloads containing query data
 * Runs queries against all Elasticsearch indices representing selected similarity models by HTTP Post requests
 * Collects Elasticsearch response objects (JSON)
 * Parse and format Elasticsearch response objects into the format required by TREC_EVAL and outputs to a file for each algorithm
 * Automates the running of the TREC_EVAL binary to generate evaluations using query relevance files and the Elasticsearch generated similarity scores 
 * Writes TREC_EVAL evaluations to files for each similarity model
 * ___________________
 * 
 * Query runtime: ~20 minutes
 * TREC_EVAL runtime: <1 minute
 * ___________________
 * parseResponse(JSONObject response, String model)
 * parseDocument(String doc)
 * buildQueries()
 * readFile(File working, Charset encoding)
 * readQueries(File queryFile)
 * readFullyAsString(InputStream inputStream, String encoding)
 * readFully(InputStream inputStream)
 * trec_eval(String binaryDir, String qrelDir, String writeDir)
 * runQueries()
 */
public class queries {
	
	public static String selectedModels[];
	static Stack<Object> documents = new Stack<Object>();
	static ArrayDeque<String> queryset = new ArrayDeque<String>();
	static ArrayDeque<JSONObject> jsonqueries = new ArrayDeque<JSONObject>();
	
	
	static int query_number = 0;
	static String q0 = "0";
	static String document_id = "";
	static int rank = 0;
	static double score = 0;
	static String trec_format = "";
	
	/*
	 * Parse JSON response object into TREC readable format
	 */
	public static void parseResponse(JSONObject response, String model) throws IOException {
		++query_number;
		rank = 0;
		
		//loop for this query
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("G:\\Project\\test\\" + model + ".txt", true), "utf-8"))) {
			for(int q = 0; q < response.getJSONObject("hits").getJSONArray("hits").length(); q++) {
				document_id = response.getJSONObject("hits").getJSONArray("hits").getJSONObject(q).getJSONObject("_source").get("UID").toString();
				++rank;
				score = (double)response.getJSONObject("hits").getJSONArray("hits").getJSONObject(q).get("_score");
				trec_format = query_number + "\t" + q0 + "\t" + document_id + " " + rank + " " + score + " Exp\n";
				System.out.println(trec_format);
				
				   writer.write(trec_format);

			}
		}
		
	}
	
	/*
	 * Sanitize queries before running or attempting to construct JSON objects
	 */
	public static String parseDocument(String doc) throws Exception {
		Pattern pattern1 = Pattern.compile("(<script.*</script>)|(<[^>]*>)|(\\\\)|(&nbsp;)|(^[\\s]+)");
		Pattern pattern2 = Pattern.compile("(?m)^(?:[\t ]*(?:\r?\n|\r))+");
		Matcher m = pattern1.matcher(doc);
		
		String postParse = m.replaceAll("");
		Matcher m2 = pattern2.matcher(postParse);
		postParse = m2.replaceAll("");
		return postParse;
	}
	
	/*
	 * Build JSON payloads for each query
	 */
	public static void buildQueries() {
		while(!queryset.isEmpty()) {
			//Map<String, Object> json = new HashMap<String, Object>();
			JSONObject query = new JSONObject();
			JSONObject match = new JSONObject();
			JSONObject content = new JSONObject();
			content.put("content", queryset.pop());
			match.put("match", content);
			query.put("query", match);
			System.out.println(query.toString());
			jsonqueries.add(query);
		}

	}
	
	/*
	 * Read all queries from query file
	 */
	static void readFile(File working, Charset encoding) throws IOException {
		
		FileReader reader;
		
		reader = new FileReader(working);

		Scanner in = new Scanner(reader);
		
		
		String line = "";
		

		while(in.hasNextLine()) {
			line = in.nextLine();
			line = line.trim();
			
			if(line.contains("<query>")) {
				line = line.replace("<query>", "").replace("</query>", "");
				queryset.add(line);
				
			}
		}
		in.close(); //As scanner no longer in use
		System.out.println("Read " + queryset.size() + " queries from file " + working.getName());
				
	}
	
	/*
	 * Passes valid query file to readFile() for parsing
	 * Controls the reading, building and running of queries
	 */
	public static void readQueries(File queryFile) throws Exception {
	

		if (queryFile.isFile()){
			System.out.println("Reading queries from file: " + queryFile.getName());
			readFile(queryFile, StandardCharsets.UTF_8);
			System.out.println("Finished - running queries");
			buildQueries();
			runQueries();
		}
		else {
			System.out.println("Not a file " + queryFile);
			JOptionPane.showMessageDialog(null, "Valid queryfile not specified");
		}


	}
	
	/*
	 * Robust method for reading HTTP connection input stream 1024 bytes at a time due to prevent overflow errors
	 */
	public static String readFullyAsString(InputStream inputStream, String encoding) throws IOException {
        return readFully(inputStream).toString(encoding);
    }

	/*
	 * Utilized by readFullyAsString() as described above
	 */
    public static ByteArrayOutputStream readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos;
    }
	
    /*
     * Takes directory of TREC_EVAL binary, query relevance file and directory to write result files to as parameters
     * Runs TREC_EVAL as a Java process via command line execution and stores resulting output in 'evaluations' directory
     */
    public static void trec_eval(String binaryDir, String qrelDir, String writeDir) throws IOException {
    	
    	File resultsDir = new File(binaryDir + "\\evaluations");
        if (!resultsDir.exists()) resultsDir.mkdirs();
    	
    	for(int q = 0; q < selectedModels.length - 1; q++) {
	    	try {
	    		System.out.println("Executing trec like this: " + binaryDir + "\\TREC_EVAL.exe " + qrelDir + "\\qrels.eng.clef2015.test.bin.txt " + writeDir + "\\" + selectedModels[q] + ".txt");
	    		String line;
	    		String output = "";
		    	Process p = Runtime.getRuntime().exec(binaryDir + "\\TREC_EVAL.exe " + qrelDir + "\\qrels.eng.clef2015.test.bin.txt " + writeDir + "\\" + selectedModels[q] + ".txt", null, new File(binaryDir));

		    	BufferedReader in = new BufferedReader(
		    			new InputStreamReader(p.getInputStream()) );
		        while ((line = in.readLine()) != null) {
		        	
		        	output += line + "\n";
		        }
		        in.close();
		        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(binaryDir + "\\evaluations\\" + selectedModels[q] + ".txt", true), "utf-8"))) {
		        	
		        
		        
			        System.out.println(output);
			        writer.write(output);
		        }
	    	}
	    	catch (Exception e) {
	    		JOptionPane.showMessageDialog(null, "Failed to run TREC_EVAL\nError: " + e + "\nEnsure that TREC_EVAL.exe is in the document directory folder");
	    		System.out.println("Error: " + e);
	    	}
    	}
    }
	
    /*
     * Runs queries by posting JSON payloads containing query data to Elasticsearch endpoint for each selected similarity algorithm
     */
	public static void runQueries() throws MalformedURLException, IOException {
		ArrayDeque<JSONObject> temporaryQueries;
		for(int q = 0; q < selectedModels.length; q++) {

			String url = "http://localhost:9200/" + selectedModels[q] + "/_search?size=200";
			
			System.out.println("Json queries: " + jsonqueries);
			
			File trec_input = new File("G:\\Project\\Test\\" + selectedModels[q] + ".txt");
			trec_input.delete();
			
			temporaryQueries = jsonqueries.clone();
			while(!temporaryQueries.isEmpty()) {
				URLConnection connection = new URL(url).openConnection();
	
				connection.setDoOutput(true); //Triggers POST

				connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

				
				System.out.println("Running query " + temporaryQueries.peek());
			
				try (OutputStream output = connection.getOutputStream()) {
					
				    output.write(temporaryQueries.pop().toString().getBytes("UTF-8"));
	
				}
				catch(Exception e) {
					JOptionPane.showMessageDialog(null, "Query running failed\nError: " + e + "\n Is Elasticsearch running? (port 9200)");
				}
				//System.out.println(readFullyAsString(connection.getInputStream(), "UTF-8"));
				JSONObject resp = new JSONObject(readFullyAsString(connection.getInputStream(), "UTF-8"));
				parseResponse(resp, selectedModels[q]);

				
				connection.getInputStream().close();
				
			}
			query_number = 0;
			//System.out.println("Finished. Indexed " + (documentIndex - 1) + " documents.");

		}

	}
	
}