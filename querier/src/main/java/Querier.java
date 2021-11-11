import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import query.QueryHandler;
import utils.StopWordGenerator;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import static constants.DirectoryConstants.INDEX_DIR;
import static constants.DirectoryConstants.RESULTS_DIR;


public class Querier {

  public static void main(String[] args) throws InterruptedException, IOException, ParseException {
    createDirs();
    StopWordGenerator stopWordGenerator = StopWordGenerator.getInstance();

    // creating a list of analysers to iterate through
    List<Analyzer> analysers = new ArrayList<>();
    //    analysers.add(new SimpleAnalyzer());
    //    analysers.add(new StandardAnalyzer());
    analysers.add(new EnglishAnalyzer(stopWordGenerator.getCharset()));
    //    analysers.add(new ClassicAnalyzer());
    //    analysers.add(new WhitespaceAnalyzer());
    //    analysers.add(
    //        new Analyzer() {
    //          @Override
    //          protected TokenStreamComponents createComponents(String s) {
    //            Tokenizer tokenizer = new NGramTokenizer(1, 3);
    //            TokenStream filter = new LowerCaseFilter(tokenizer);
    //            return new TokenStreamComponents(tokenizer, filter);
    //          }
    //        });

    // creating a list of similarities
    List<Similarity> similarities = new ArrayList<>();
    //    similarities.add(new ClassicSimilarity());
    similarities.add(new BM25Similarity(1.2f, 0.8f));
    //    similarities.add(new LMDirichletSimilarity(1500));
    //    similarities.add(
    //        new MultiSimilarity(new Similarity[] {new BM25Similarity(), new AxiomaticF1LOG()}));
    //    similarities.add(new AxiomaticF1EXP());
    //    similarities.add(new AxiomaticF1LOG());
    //    similarities.add(new AxiomaticF2EXP());

    for (Analyzer analyser : analysers) {
      for (Similarity similarity : similarities) {
        // creating index for the particular analyser and similarity
        Parser parser = new Parser(analyser, similarity);
        parser.parseAndIndex();

        // waiting 1 second before querying
        TimeUnit.SECONDS.sleep(1);
        // Querying the index using the same analyser/similarity pair
        QueryHandler queryHandler = new QueryHandler(analyser, similarity, 1000);
        queryHandler.executeQueries();

      }
    }
  }

  private static void createDirs() {
    // probably need a better create method lol
    boolean indexDirectory = new File(INDEX_DIR).mkdir();
    boolean resultsDirectory = new File(RESULTS_DIR).mkdir();
  }
}
