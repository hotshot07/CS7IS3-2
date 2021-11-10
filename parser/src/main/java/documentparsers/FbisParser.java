package documentparsers;

import constants.FbisTags;
import constants.FbisTagsData;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class FbisParser {

  private static BufferedReader br;
  private static final String[] IGNORE_FILES = {"readchg.txt", "readmefb.txt"};

  private final String DIR_PATH;
  private final IndexWriter indexWriter;

  public FbisParser(IndexWriter indexWriter, String DIR_PATH) {
    this.indexWriter = indexWriter;
    this.DIR_PATH = DIR_PATH + "/fbis";
  }

  public void parseAndIndexDocs() throws IOException {
    loadFBISDocs(this.DIR_PATH, this.indexWriter);
  }

  private void loadFBISDocs(String fbisDirectory, IndexWriter iwr) throws IOException {
    Directory dir = FSDirectory.open(Paths.get(fbisDirectory));

    for (String fbisFile : dir.listAll()) {
      if (!fbisFile.equals(IGNORE_FILES[0]) && !fbisFile.equals(IGNORE_FILES[1])) {
        br = new BufferedReader(new FileReader(fbisDirectory + "/" + fbisFile));
        System.out.println(fbisDirectory + "/" + fbisFile);
        indexDocumentsFromFile(iwr);
      }
    }
  }

  private void indexDocumentsFromFile(IndexWriter iwr) throws IOException {
    String file = readAFile();
    org.jsoup.nodes.Document document = Jsoup.parse(file);

    List<Element> list = document.getElementsByTag("doc");
    for (Element doc : list) {

      FbisTagsData fbisData = new FbisTagsData();
      if (doc.getElementsByTag(FbisTags.DOCNO.name()) != null)
        fbisData.setDocNum(removeUnnecessaryTags(doc, FbisTags.DOCNO));
      if (doc.getElementsByTag(FbisTags.TEXT.name()) != null)
        fbisData.setText(removeUnnecessaryTags(doc, FbisTags.TEXT));
      if (doc.getElementsByTag(FbisTags.TI.name()) != null)
        fbisData.setTi(removeUnnecessaryTags(doc, FbisTags.TI));
      fbisData.setAll(fbisData.getText() + " " + fbisData.getTi().trim());
      createFBISDocument(fbisData, iwr);
    }
  }

  private String removeUnnecessaryTags(Element doc, FbisTags tag) {

    Elements element = doc.getElementsByTag(tag.name());
    Elements tempElement = element.clone();
    // Remove any nested
    deleteNestedTags(tempElement, tag);
    String data = tempElement.toString();
    // Remove any instance of "\n"
    if (data.contains("\n")) data = data.replaceAll("\n", "").trim();
    // Remove start and end tags
    if (data.contains(("<" + tag.name() + ">").toLowerCase()))
      data = data.replaceAll("<" + tag.name().toLowerCase() + ">", "").trim();
    if (data.contains(("</" + tag.name() + ">").toLowerCase()))
      data = data.replaceAll("</" + tag.name().toLowerCase() + ">", "").trim();

    data = data.trim().replaceAll(" +", " ");
    return data;
  }

  private void deleteNestedTags(Elements element, FbisTags currTag) {

    for (FbisTags tag : FbisTags.values()) {
      if (element.toString().contains("<" + tag.name().toLowerCase() + ">")
          && element.toString().contains("</" + tag.name().toLowerCase() + ">")
          && !tag.equals(currTag)) {
        element.select(tag.toString()).remove();
      }
    }
  }

  private Document createFBISDocument(FbisTagsData fbisData, IndexWriter iwr) {
    Document document = new Document();
    document.add(new StringField("docno", fbisData.getDocNum(), Field.Store.YES));
    document.add(new TextField("title", fbisData.getTi(), Field.Store.YES));
    document.add(new TextField("content", fbisData.getAll(), Field.Store.YES));
    try {
      iwr.addDocument(document);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return document;
  }

  private String readAFile() throws IOException {
    try {
      StringBuilder sb = new StringBuilder();
      String line = br.readLine();

      while (line != null) {
        sb.append(line);
        sb.append("\n");
        line = br.readLine();
      }
      return sb.toString();
    } finally {
      br.close();
    }
  }
}
