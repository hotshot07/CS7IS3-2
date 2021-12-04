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
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

import static constants.DirectoryConstants.*;
import static utils.CommonUtils.replacePunctuation;

public class QueryHandler {
  private Analyzer analyzer;
  private Similarity similarity;
  private int max_results;
  private final ArrayList<LinkedHashMap<String, String>> parsedQueries;
  private final LinkedHashMap<String, String> refinedQueries;

  private static final String RELEVANT_PHRASES_REGEX = "a relevant document will focus|a relevant document identifies|a relevant document could|a relevant document may|a relevant document must|a relevant document will|a document will|to be relevant|relevant documents|a document must|relevant|will contain|will discuss|will provide|must cite";

  private static final String IRRELEVANT_PHRASES_REGEX = "are also not relevant|are not relevant|are irrelevant|is not relevant|not|NOT";

  public QueryHandler() {
    QueryFileParser queryFileParser = new QueryFileParser(TOPIC_PATH);
    this.parsedQueries = queryFileParser.parseQueryFile();

    LinkedHashMap<String, String> refinedQueries = new LinkedHashMap<>();
    for (LinkedHashMap<String, String> query : parsedQueries) {
      String queryString = refineQueryComponents(query);
      refinedQueries.put(query.get("queryID"),queryString);
    }

    this.refinedQueries = refinedQueries;
  }

  public void configure(Analyzer analyzer, Similarity similarity, int max_results){
    this.analyzer = analyzer;
    this.similarity = similarity;
    this.max_results = max_results;
  }

  public void executeQueries() throws IOException, ParseException {

    long start_time = System.currentTimeMillis();
    // Configure the index details to query
    IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIR)));
    IndexSearcher indexSearcher = new IndexSearcher(indexReader);
    indexSearcher.setSimilarity(similarity);

    // TODO: figure out booster values
    HashMap<String, Float> booster = new HashMap<String, Float>();
    booster.put("title", 0.07F);
    booster.put("content", 1.1F);

    MultiFieldQueryParser indexParser =
        new MultiFieldQueryParser(new String[] {"title", "content"}, analyzer, booster);

    String filename =
        RESULTS_DIR
            + "/results_"
            + analyzer.getClass().getSimpleName().toLowerCase(Locale.ROOT)
            + "_"
            + similarity.getClass().getSimpleName().toLowerCase(Locale.ROOT);

    PrintWriter resultsWriter = new PrintWriter(filename, StandardCharsets.UTF_8);


    for (String queryId : refinedQueries.keySet()) {

      String queryString = refinedQueries.get(queryId);
      Query finalQuery = indexParser.parse(QueryParser.escape(queryString));

      TopDocs results = indexSearcher.search(finalQuery, max_results);
      ScoreDoc[] hits = results.scoreDocs;
      // To write the results for each hit in the format expected by the trec_eval tool.
      for (int i = 0; i < hits.length; i++) {
        Document document = indexSearcher.doc(hits[i].doc);
        resultsWriter.println(
            queryId
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
            + " milliseconds\n",
        filename);
  }

  private String refineQueryComponents(LinkedHashMap<String, String> query) {

    StringBuilder finalQueryString = new StringBuilder();
    String title = replacePunctuation(query.get("title"));
    String desc = replacePunctuation(query.get("description"));
    String relevantNarrative = processNarrativeTag(query.get("narrative"))[0];

    return finalQueryString
            .append(title)
            .append(" ")
            .append(desc)
            .append(" ")
            .append(relevantNarrative)
            .toString();
  }

  private String[] processNarrativeTag(String narrativeString) {
    StringBuilder relevantNarrative = new StringBuilder();
    StringBuilder irrelevantNarrative = new StringBuilder();
    String[] processedNarrative = new String[2];

    BreakIterator breakIterator = BreakIterator.getSentenceInstance();
    breakIterator.setText(narrativeString);
    int index = 0;
    while (breakIterator.next() != BreakIterator.DONE) {
      String sentence = narrativeString.substring(index, breakIterator.current());

      if (!sentence.contains("not relevant") && !sentence.contains("irrelevant")) {
        relevantNarrative.append(sentence.replaceAll(RELEVANT_PHRASES_REGEX,""));
      } else {
        irrelevantNarrative.append(sentence.replaceAll(IRRELEVANT_PHRASES_REGEX, ""));
      }
      index = breakIterator.current();
    }
    processedNarrative[0] = relevantNarrative.toString();
    processedNarrative[1] = irrelevantNarrative.toString();
    return processedNarrative;
  }
}
