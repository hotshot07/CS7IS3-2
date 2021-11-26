import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.similarities.AxiomaticF2EXP;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.MultiSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import query.QueryHandler;
import utils.StopWordGenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static constants.DirectoryConstants.INDEX_DIR;
import static constants.DirectoryConstants.RESULTS_DIR;

public class Querier {

  public static void main(String[] args) throws InterruptedException, IOException, ParseException {
    createDirs();
    StopWordGenerator stopWordGenerator = StopWordGenerator.getInstance();

    // creating a list of analysers to iterate through
    List<Analyzer> analysers = new ArrayList<>();
    //    analysers.add(new SimpleAnalyzer());
    //    analysers.add(new StandardAnalyzer(stopWordGenerator.getCharset()));
    analysers.add(new EnglishAnalyzer(stopWordGenerator.getCharset()));
    //    analysers.add(new ClassicAnalyzer(stopWordGenerator.getCharset()));
    //    analysers.add(new WhitespaceAnalyzer());

    //    analysers.add(
    //        new Analyzer() {
    //          @Override
    //          protected TokenStreamComponents createComponents(String s) {
    //            WikipediaTokenizer src = new WikipediaTokenizer();
    //            TokenStream result = new LowerCaseFilter(src);
    //            result = new StopFilter(result, stopWordGenerator.getCharset());
    //            result = new PorterStemFilter(result);
    //            result = new LowerCaseFilter(result);
    //            return new TokenStreamComponents(src, result);
    //          }
    //        });

    // creating a list of similarities
    List<Similarity> similarities = new ArrayList<>();
    //    similarities.add(new ClassicSimilarity());
    similarities.add(new BM25Similarity(0.65F, 0.8F));
    //    similarities.add(new LMDirichletSimilarity(1500));
    similarities.add(
        new MultiSimilarity(
            new Similarity[] {new BM25Similarity(1F, 0.95F), new AxiomaticF2EXP()}));
    //    similarities.add(new AxiomaticF1EXP());
    //    similarities.add(new AxiomaticF1LOG());
    //    similarities.add(new AxiomaticF2EXP());
    // similarities.add(new AxiomaticF2LOG());

    // analyser was simple bm25 for parser

    Parser parser =
        new Parser(new EnglishAnalyzer(stopWordGenerator.getCharset()), new BM25Similarity());
    parser.parseAndIndex();
    //    //
    //
    TimeUnit.SECONDS.sleep(1);
    // Querying the index using the same analyser/similarity pair
    QueryHandler queryHandler =
        new QueryHandler(
            new EnglishAnalyzer(stopWordGenerator.getCharset()),
            new MultiSimilarity(
                new Similarity[] {new BM25Similarity(1.2F, 0.95F), new AxiomaticF2EXP()}),
            1000);
    queryHandler.executeQueries();

    //    for (Analyzer analyser : analysers) {
    //      for (Similarity similarity : similarities) {
    //        // creating index for the particular analyser and similarity
    //        Parser parser = new Parser(analyser, similarity);
    //        parser.parseAndIndex();
    //
    //        // waiting 1 second before querying
    //        TimeUnit.SECONDS.sleep(1);
    //        // Querying the index using the same analyser/similarity pair
    //        QueryHandler queryHandler = new QueryHandler(analyser, similarity, 1000);
    //        queryHandler.executeQueries();
    //      }
    //    }
  }

  private static void createDirs() {
    // probably need a better create method lol
    boolean indexDirectory = new File(INDEX_DIR).mkdir();
    boolean resultsDirectory = new File(RESULTS_DIR).mkdir();
  }
}
