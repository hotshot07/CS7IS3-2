package documentparsers;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import org.apache.lucene.document.Document;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LatimesDocumentParser {
  String DIR_PATH = "data/data/latimes/";
  IndexWriter iwriter;

  public LatimesDocumentParser(IndexWriter indexWriter) {
    this.iwriter = indexWriter;
  }

  public void parseDocuments() throws IOException {
    File directoryPath = new File(DIR_PATH);
    List<File> filesList =
        Arrays.stream(Objects.requireNonNull(directoryPath.listFiles()))
            .filter(file -> !file.toString().toLowerCase(Locale.ROOT).endsWith(".txt"))
            .collect(Collectors.toList());

    for (File file : filesList) {
      org.jsoup.nodes.Document fileToParse = Jsoup.parse(file, "UTF-8");
      List<Element> documentsInFile = fileToParse.select("doc");

      for (Element element : documentsInFile) {
        List<Node> nodes =
            element.childNodes().stream()
                .filter(node -> !node.toString().equals(" "))
                .collect(Collectors.toList());
        iwriter.addDocument(createDocument(nodes));
      }
    }
  }

  private Document createDocument(List<Node> nodes) {
    Document document = new Document();
    for (Node node : nodes) {
      // do additional processing before indexing as per requirement
      document.add(
          new TextField(((Element) node).tagName(), ((Element) node).text(), Field.Store.YES));
    }
    return document;
  }
}
