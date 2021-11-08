package query;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import queryparser.QueryFileParser;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

public class QueryHandler {
  private final Analyzer analyzer;
  private final Similarity similarity;
  private final int max_results;

  public QueryHandler(Analyzer analyzer, Similarity similarity, int max_results) {
    this.analyzer = analyzer;
    this.similarity = similarity;
    this.max_results = max_results;
  }

  public void executeQueries() throws IOException, ParseException {

    long start_time = System.currentTimeMillis();
    // Configure the index details to query
    IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get("data/index")));
    IndexSearcher indexSearcher = new IndexSearcher(indexReader);
    indexSearcher.setSimilarity(similarity);

    MultiFieldQueryParser indexParser =
        new MultiFieldQueryParser(new String[] {"title", "content"}, analyzer);

    String filename =
        "Results/results_"
            + analyzer.getClass().getSimpleName().toLowerCase(Locale.ROOT)
            + "_"
            + similarity.getClass().getSimpleName().toLowerCase(Locale.ROOT);

    PrintWriter resultsWriter = new PrintWriter(filename, StandardCharsets.UTF_8);

    QueryFileParser queryFileParser = new QueryFileParser("data/queries");
    ArrayList<LinkedHashMap<String, String>> parsedQueries = queryFileParser.parseQueryFile();

    int queryID = 0;
    for (LinkedHashMap<String, String> query : parsedQueries) {
      // preparing query here
      // String queryString = prepareQueryString(query);

      Query finalQuery = indexParser.parse(QueryParser.escape("queryString"));

      TopDocs results = indexSearcher.search(finalQuery, max_results);
      ScoreDoc[] hits = results.scoreDocs;

      // To write the results for each hit in the format expected by the trec_eval tool.
      for (int i = 0; i < hits.length; i++) {
        Document document = indexSearcher.doc(hits[i].doc);
        resultsWriter.println(
            ++queryID
                + " Q0 "
                + document.get("docno")
                + " "
                + i
                + " "
                + hits[i].score
                + " HYLIT"); // HYLIT - Have You Lucene It?
      }
    }

    resultsWriter.close();
    indexReader.close();

    System.out.format(
        "Result file %s generated in "
            + (System.currentTimeMillis() - start_time)
            + " milliseconds");
  }
}

//  private static String prepareQueryString(LinkedHashMap<String, String> query) {
//    StringBuilder queryString = new StringBuilder();
//    // Need to perform query expansion and query refinement in this function. Default query
//    // generation for Phase 1.
//    queryString.append(query.get("title"));
//    queryString.append(query.get("description"));
//    return queryString.toString();
//  }
//
//  private static void processNarativeTAG(LinkedHashMap<String, String> query) {
//    StringBuilder additionalDataToAppend = new StringBuilder();
//    StringBuilder removeDataFromQuery = new StringBuilder();
//
//    // Todo: Decide on the way to parse the narrative tag, find the relevant details to append to
//    // the query and remove any non relevant words.
//    // Update the current query.
//  }
