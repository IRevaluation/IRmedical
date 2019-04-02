import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.jsoup.Jsoup;


/*
 * parseDocuments.java
 * -------------------
 * Reads all .dat files in document directory
 * Parses each document to remove special characters, HTML, Javascript, etc. that cannot be indexed
 * Extracts necessary data and constructs JSON payloads
 * Sends JSON payloads via HTTP Post requests to Elasticsearch Index API to index the documents
 * ___________________
 * 
 * Runtime: ~26 hours (once only)
 * 
 * ___________________
 * readFile(File working, Charset encoding)
 * parseDocument(String doc)
 * checkDirWithUrls(File dir, String index)
 * indexDocuments(String index)
 */
public class parseDocuments {
	
	private int counter;
	private int documentIndex = 1;
	Stack<Object> documents = new Stack<Object>();
	
	
	/*
	 * Sanitize document content to ensure no errors when indexing
	 */
	public static String parseDocument(String doc) throws Exception {
		String pattern = ("(<script.*</script>)|(<[^>]*>)|(\\\\)|(&nbsp;)|(^[\\s]+)");
		Pattern r = Pattern.compile(pattern, Pattern.MULTILINE);
		Matcher m = r.matcher(doc);
		String postParse = m.replaceAll("");//.replaceAll("(?m)^(?:[\t ]*(?:\r?\n|\r))+","");
		return postParse;
	}
	
	/*
	 * Read all documents from a file, constructing JSON payload for each document and some additional parsing to ensure valid JSON objects are created
	 */
	void readFile(File working, Charset encoding) throws IOException {
		
		FileReader reader;
		
		reader = new FileReader(working);
		Scanner in = new Scanner(reader);
		
		
		String line = "";
		
		StringBuilder sbuild = new StringBuilder();
		Map<String, Object> json = new HashMap<String, Object>();

		
		while(in.hasNextLine()) {
			line = in.nextLine();
			line = line.trim();
			
			if(line.contains("#UID:")) {
				//Map<String, Object> json = new HashMap<String, Object>();
				json.put("UID", line.replace("#UID:", ""));
				
				in.nextLine();
				
				line = in.nextLine();
				json.put("URL", line.replace("#URL:", ""));
				
				in.nextLine();
				
				line = in.nextLine();
				
				
				while(!line.equals("#EOR")){  //keep looping until have read all this webpages text

					sbuild.append(line);
					
					line = in.nextLine();

				}

				String stripped_content = parse(sbuild.toString());

					
				if(!stripped_content.equals(" +")){  
					json.put("content", stripped_content);
				}
				
				JSONObject myjson = new JSONObject(json);
				System.out.println(myjson.toString());

				documents.push(myjson);
				
				sbuild.setLength(0);
				stripped_content = "";
				json.clear();
				
			}
		}
		
		in.close();
		reader.close();

	}

	public static String parse(String parseString) {
		
		String stripped_content = Jsoup.parse(parseString).text();
		stripped_content = stripped_content.replaceAll("\\<.*?>","");
		stripped_content = stripped_content.replaceAll("\\\\", "");
		stripped_content = stripped_content.replaceAll(" \\+", " ");
		stripped_content = stripped_content.trim();
		
		return stripped_content;
		
	}
	
	/*
	 * Checks dataset directory for valid .dat files containing documents and passes to the above readFile() method
	 * Kicks off indexing once all files have been processed
	 */
	public void checkDirWithUrls(File dir, String index) throws Exception {

		for (File child : dir.listFiles()) {
			
			if (".".equals(child.getName()) || "..".equals(child.getName()))
				continue; // Ignore the self and parent aliases
			if (child.isFile()){
				counter++;
				System.out.println("File " + counter + ": " + child.getName());
				readFile(child, StandardCharsets.UTF_8);
				
				System.out.println("Finished - indexing documents");
				indexDocuments(index);
				documents.empty();
			}
			else if (child.isDirectory()){
				System.out.println(child);
				checkDirWithUrls(child, index);
			}	
			
		}

	}
	
	/*
	 * Connects to Elasticsearch host and sends HTTP Post requests containing JSON-structured document data to the appropriate Elasticsearch index
	 */
	public boolean indexDocuments(String index) throws MalformedURLException, IOException {

		String url = "http://localhost:9200/" + index + "/documents/";
		
		
		while(!documents.isEmpty()) {
			URLConnection connection = new URL(url + documentIndex).openConnection();

			connection.setDoOutput(true); //Triggers POST
			connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			
			System.out.println("Indexing document " + documentIndex);
			++documentIndex;
		
			try (OutputStream output = connection.getOutputStream()) {
			    output.write(documents.pop().toString().getBytes("UTF-8"));
			}
			catch (Exception e) {
				return false;
			}
			connection.getInputStream().close();

			
		}
		System.out.println("Finished. Indexed " + (documentIndex - 1) + " documents.");

		return true;
	}
	
}