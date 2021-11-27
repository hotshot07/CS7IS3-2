package query;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import queryparser.QueryFileParser;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.text.BreakIterator;

import static constants.DirectoryConstants.*;
import static utils.CommonUtils.replacePunctuation;

public class QueryHandler {
  private final Analyzer analyzer;
  private final Similarity similarity;
  private final int max_results;

  private static final String RELEVANT_PHRASES_REGEX = "a relevant document will focus|a relevant document identifies|a relevant document could|a relevant document may|a relevant document must|a relevant document will|a document will|to be relevant|relevant documents|a document must|relevant|will contain|will discuss|will provide|must cite";

  private static final String IRRELEVANT_PHRASES_REGEX = "are also not relevant|are not relevant|are irrelevant|is not relevant|not|NOT";

  public QueryHandler(Analyzer analyzer, Similarity similarity, int max_results) {
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
    HashMap<String, Float> booster = new HashMap<>();
    booster.put("title", 0.50f);
    booster.put("content", 1.1f);

    MultiFieldQueryParser indexParser =
        new MultiFieldQueryParser(new String[] {"title", "content"}, analyzer, booster);

    String filename =
        RESULTS_DIR
            + "/results_"
            + analyzer.getClass().getSimpleName().toLowerCase(Locale.ROOT)
            + "_"
            + similarity.getClass().getSimpleName().toLowerCase(Locale.ROOT);

    PrintWriter resultsWriter = new PrintWriter(filename, String.valueOf(StandardCharsets.UTF_8));

    QueryFileParser queryFileParser = new QueryFileParser(TOPIC_PATH);
    ArrayList<LinkedHashMap<String, String>> parsedQueries = queryFileParser.parseQueryFile();

    for (LinkedHashMap<String, String> query : parsedQueries) {
      HashMap<String,String> queryComponents = refineQueryComponents(query);
      
      BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();

      //Simple seperate queries for title and description
      //TODO: Need to tune the hyperparameters
      Query titleQuery = indexParser.parse(QueryParser.escape(queryComponents.get("title")));
      booleanQuery.add(new BoostQuery(titleQuery, 4f), BooleanClause.Occur.SHOULD);

      Query descriptionQuery = indexParser.parse(QueryParser.escape(queryComponents.get("description")));
      booleanQuery.add(new BoostQuery(descriptionQuery, 1.7f), BooleanClause.Occur.SHOULD);

      if(!queryComponents.get("relevantNarrative").isEmpty()) {
          Query relevantNarrativeQuery = indexParser.parse(queryComponents.get("relevantNarrative"));
          booleanQuery.add(new BoostQuery(relevantNarrativeQuery, 2.5f), BooleanClause.Occur.MUST);
      }

      if(!queryComponents.get("irrelevantNarrative").isEmpty()){
          Query irrelevantNarrativeQuery = indexParser.parse(queryComponents.get("irrelevantNarrative"));
          booleanQuery.add(new BoostQuery(irrelevantNarrativeQuery, 2.0f), BooleanClause.Occur.FILTER);
      }

      TopDocs results = indexSearcher.search(booleanQuery.build(), max_results);
      ScoreDoc[] hits = results.scoreDocs;
      String queryId = query.get("queryID");
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

  private HashMap<String,String> refineQueryComponents(LinkedHashMap<String, String> query) {
    HashMap<String,String> queryComponents = new HashMap<>();

    //replace punctuation for title and description
    queryComponents.put("title",replacePunctuation(query.get("title")));
    queryComponents.put("description",replacePunctuation(query.get("description")));

    //Process the narrative tag and split into relevant and irrelevant narratives. Relevant: Query Augmentation, irrelevant: Query Refinement
    String[] processedNarrative = processNarrativeTag(query.get("narrative"));
    queryComponents.put("relevantNarrative",replacePunctuation(processedNarrative[0]));
    queryComponents.put("irrelevantNarrative",replacePunctuation(processedNarrative[1]));
    return queryComponents;
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
