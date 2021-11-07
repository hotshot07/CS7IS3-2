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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.similarities.*;

public class QueryHandler {
	private static String TOP_TAG_START = "<top>";
	private static String TOP_TAG_END = "</top>";
	private static String NUM_TAG = "<num>";
	private static String TITLE_TAG = "<title>";
	private static String DESC_TAG = "<desc>";
	private static String NARR_TAG = "<narr>";

	private ArrayList<Analyzer> analyzers;
	private ArrayList<Similarity> similarities;
	private ArrayList<HashMap<String,String>> parsedQueries;
	private String queryFilePath;
	private Sting corpusName;
	private static String RESULT_FILENAME_FORMAT = "RESULT_{}_{}.txt";

	/*
	Suggested Usage:
	
	QueryHandler queryHandler = new QueryHandler(indexpath,queryFilePath,analyzersList,similaritiesList);
	queryHandler.parseQueryFile();
	queryHandler.executeQueries();
	
	*/

	QueryHandler(String indexPath,String filePath,ArrayList<Analyzer> analyzers,ArrayList<Similarity> similarities){
		this.indexPath = indexPath;
		this.queryFilePath = filePath;
		this.analyzers = analyzers;
		this.similarities = similarities;
		parsedQueries = new ArrayList<HashMap<String,String>>();
	}
	
	/**
	 * Reads the topics query file and parses the 50 queries
	 * The queries are stored in a Arraylist of HashMaps.
	 * 
	 * @param filePath
	 * @return
	 */
	public void parseQueryFile() {
		try {
			BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(this.queryFilePath), StandardCharsets.UTF_8);
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
                	this.parsedQueries.add(newQuery);
            		currentLine = bufferedReader.readLine();
            	}
            }

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void executeQueries(){

		//Configure the index details to query
		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(this.indexPath)));
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		
		for(Analyzer analyzer: this.analyzers) {

			//common fields to be used across the document types - id -> <DOCNO> , title -> <HEADER></H2> and content -> <TEXT>
			MultiFieldQueryParser indexParser = new MultiFieldQueryParser(new String[]{"title", "content"}, analyzer);

			for (Similarity similarity : this.similarities) {
				Date startTime = new Date();

				//set the current similarity for the analyzer
				indexSearcher.setSimilarity(similarity);

				//Create a new result path for the current combination: STORED in the FORMAT RESULT_ANALYZER_SIMILARITY.txt
				String resultPath = String.format(RESULT_FILENAME_FORMAT, analyzer,similarity);
				PrintWriter resultsWriter = new PrintWriter(resultPath, "UTF-8");

				int queryID = 0;
				for(HashMap<String,String> query:parsedQueries.keySet()){
					String queryString = prepareQueryString(query);

					Query finalQuery = indexParser.parse(QueryParser.escape(queryString));

					TopDocs results = indexSearcher.search(finalQuery, 1000);
					ScoreDoc[] hits = results.scoreDocs;

					// To write the results for each hit in the format expected by the trec_eval tool.
					for (int i = 0; i < hits.length; i++) {
						Document document = searcher.doc(hits[i].doc);
						resultsWriter.println(++queryID + " Q0 " + doc.get("id") + " " + i + " " + hits[i].score + " HYLIT"); //HYLIT - Have You Lucene It?
					}
				}

				resultsWriter.close();
				indexReader.close();
				Date endTime = new Date();
				System.out.println("Result generated in "+(endTime.getTime() - startTime.getTime()) +" milliseconds");
    			System.out.println("=========================================================");
			}
		}
	}

	private static String prepareQueryString(HashMap<String,String> query){
		StringBuilder queryString = new StringBuilder();
		//Need to perform query expansion and query refinement in this function. Default query generation for Phase 1.
		queryString.append(query.get("title"));
		queryString.append(query.get("description"));
		return queryString.toString();
	}

	private static void processNarativeTAG(HashMap<String,String> query){
		StringBuilder additionalDataToAppend = new StringBuilder();
		StringBuilder removeDataFromQuery = new StringBuilder();

		//Todo: Decide on the way to parse the narrative tag, find the relevant details to append to the query and remove any non relevant words.
		//Update the current query. 
	}

	
}
