package documentparsers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class LatimesDocumentParser {
  String FILE_PATH = "data/data/latimes/";
  private final Analyzer analyzer;
  private final Similarity similarity;

  public LatimesDocumentParser(Analyzer analyzer, Similarity similarity){
    this.analyzer = analyzer;
    this.similarity = similarity;
  }

  public void parseDocument() throws IOException {
    File directoryPath = new File(FILE_PATH);
    List<File> filesList = Arrays.stream(Objects.requireNonNull(directoryPath.listFiles())).filter(file -> !file.toString().toLowerCase(Locale.ROOT).endsWith(".txt")).collect(Collectors.toList());

    System.out.println(this.analyzer);
    System.out.println(this.similarity);
    for( File file: filesList){
      Document fileToParse = Jsoup.parse(file, "UTF-8");
      List<Element> documents =  fileToParse.select("doc");

      for(Element element: documents){
        List<Node> elements =  element.childNodes().stream().filter(node -> !node.toString().equals(" ")).collect(Collectors.toList());

        for(Node node: elements){
          // CODE TO INDEX
//          System.out.println(((Element) node).tagName());
//          System.out.println(((Element) node).text());
        }
        break;
      }
      break;
    }
  }

  public static void main(String[] args) throws IOException {
    LatimesDocumentParser latimesDocumentParser =  new LatimesDocumentParser(new EnglishAnalyzer(), new BM25Similarity(1.2f, 0.8f));
    latimesDocumentParser.parseDocument();
  }
}
