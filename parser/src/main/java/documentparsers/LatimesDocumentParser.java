package documentparsers;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static utils.Utils.replacePunctuation;

public class LatimesDocumentParser {
  private final String DIR_PATH = "data/data/latimes/";
  private final IndexWriter iwriter;

  private final Set<String> irrelevantTags =
      Set.of("docid", "date", "dateline", "length", "correction-date");

  public LatimesDocumentParser(IndexWriter indexWriter) {
    this.iwriter = indexWriter;
  }

  public void parseDocuments() throws IOException {
    File directoryPath = new File(DIR_PATH);
    List<File> filesList =
        Arrays.stream(Objects.requireNonNull(directoryPath.listFiles()))
            .filter(file -> !file.toString().toLowerCase(Locale.ROOT).endsWith(".txt"))
            .collect(Collectors.toList());

    int totalCount = 0;
    for (File file : filesList) {
      org.jsoup.nodes.Document fileToParse = Jsoup.parse(file, "UTF-8");
      List<Element> documentsInFile = fileToParse.select("doc");

      int count = 0;
      for (Element element : documentsInFile) {
        List<Node> nodes =
            element.childNodes().stream()
                .filter(node -> !node.toString().equals(" "))
                .collect(Collectors.toList());
        iwriter.addDocument(createDocument(nodes));
        count++;
      }
      System.out.format("Indexed %s documents from file %s\n", count, file);
      totalCount += count;
    }
    System.out.format("Indexed %s documents in total\n", totalCount);
  }

  private Document createDocument(List<Node> nodes) {
    Document document = new Document();
    for (Node node : nodes) {
      // do additional processing before indexing as per requirement
      String tag = ((Element) node).tagName().strip();
      String text = ((Element) node).text().strip();

      // if tag is not empty AND text is not empty AND tag is not part of irrelevantTags set
      if (!tag.equals("") && !text.equals("") && !irrelevantTags.contains(tag)) {
        document.add(new TextField(tag, replacePunctuation(text), Field.Store.YES));
      }
    }

    return document;
  }
}
