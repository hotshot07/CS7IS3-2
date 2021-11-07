package Query;
/**
 * TODO:

1. Add correct package name after integrating with the main project
2. Before adding the values to the newQuery => do preprocessing like removing stopwords and punctuation methods

3. Discuss the util method folder structure, should util have methods 
that removes the stop words punctuations or each Document parsers. 

Will it be better if we have structure: DocumentParsers/*,Query/*,util.java,Parser.java ?

**/
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.StringBuilder;

public class QueryHandler {
	private static String TOP_TAG_START = "<top>";
	private static String TOP_TAG_END = "</top>";
	private static String NUM_TAG = "<num>";
	private static String TITLE_TAG = "<title>";
	private static String DESC_TAG = "<desc>";
	private static String NARR_TAG = "<narr>";
	
	/**
	 * Reads the topics query file and parses the 50 queries
	 * The queries are stored in a Arraylist of HashMaps.
	 * 
	 * @param filePath
	 * @return
	 */
	public ArrayList<HashMap<String,String>> parseQueryFile(String filePath) {
		ArrayList<HashMap<String,String>> parsedQueries = new ArrayList<HashMap<String,String>>();
		try {
			BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8);
			String currentLine = bufferedReader.readLine();
			
			HashMap<String,String> newQuery = new HashMap<String,String>();
            
            while (currentLine != null) {
            	
            	if(currentLine.isEmpty())
            		currentLine = bufferedReader.readLine();
            	
            	if(currentLine.startsWith(TOP_TAG_START)) {
            		newQuery = new HashMap<String,String>();
            		currentLine = bufferedReader.readLine();
            	}
            	
            	if (currentLine.startsWith(NUM_TAG)) {
                    String[] values = currentLine.trim().split("\\s");
                    newQuery.put("queryID",values[values.length-1]);
                    currentLine = bufferedReader.readLine();
                }
                
                if (currentLine.startsWith(TITLE_TAG)) {
                    String title = currentLine.substring(TITLE_TAG.length());
                    newQuery.put("title",title.trim());
                    currentLine = bufferedReader.readLine();
                }
                
                if (currentLine.startsWith(DESC_TAG)) {
                    currentLine = bufferedReader.readLine();
                    StringBuilder description = new StringBuilder();
                    while (currentLine != null && !currentLine.startsWith(NARR_TAG)) {
                    	description.append(currentLine+"\n");
                        currentLine = bufferedReader.readLine();
                    }
                    newQuery.put("description",description.toString().trim());
                }
                
                if (currentLine.startsWith(NARR_TAG)) {
                    currentLine = bufferedReader.readLine();
                    StringBuilder narrative = new StringBuilder();
                    while (currentLine != null && !currentLine.startsWith(TOP_TAG_END)) {
                    	narrative.append(currentLine+"\n");
                        currentLine = bufferedReader.readLine();
                    }
                    newQuery.put("narrative",narrative.toString().trim());
                }
                
                if(currentLine.startsWith(TOP_TAG_END)) {
                	parsedQueries.add(newQuery);
            		currentLine = bufferedReader.readLine();
            	}
            }

		} catch (IOException e) {
			e.printStackTrace();
		}
		return parsedQueries;
	}

	private static void processNarativeTAG(HashMap<String,String> query){
		StringBuilder additionalDataToAppend = new StringBuilder();
		StringBuilder removeDataFromQuery = new StringBuilder();

		//Todo: Decide on the way to parse the narrative tag, find the relevant details to append to the query and remove any non relevant words.
		//Update the current query. 
	}
}
