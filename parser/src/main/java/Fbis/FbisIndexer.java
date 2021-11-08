package fbisData;
	
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FbisIndexer {

    private static BufferedReader br;
    private static List<Document> fbisDocList = new ArrayList<>();
    private static final String[] IGNORE_FILES = {"readchg.txt", "readmefb.txt"};
    static String indexDirectory = "index";

    public FbisIndexer(String indexPath) throws IOException {
        try {
            System.out.println("Indexing it to'" + indexDirectory + "'...");
		
		//Path to index directory for English Analyzer
            Directory dir = FSDirectory.open(Paths.get(indexDirectory ));
            Analyzer analyzer = new EnglishAnalyzer();
            IndexWriterConfig iwconfig = new IndexWriterConfig(analyzer);

            iwconfig.setOpenMode(OpenMode.CREATE);

            IndexWriter iwr = new IndexWriter(dir, iwconfig);
            loadFBISDocs("Assignment2/fbis", iwr);

            iwr.forceMerge(1);

            iwr.close();

        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public List<Document> loadFBISDocs(String fbisDirectory, IndexWriter iwr) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(fbisDirectory));
        System.out.println(dir);
        for (String fbisFile : dir.listAll()) {
            if (!fbisFile.equals(IGNORE_FILES[0]) && !fbisFile.equals(IGNORE_FILES[1])) {
                br = new BufferedReader(new FileReader(fbisDirectory + "/" + fbisFile));
                System.out.println(fbisDirectory + "/" + fbisFile);
                indexDocumentsFromFile(iwr);
            }
        }
        return fbisDocList;
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

    private static String removeUnnecessaryTags(Element doc, FbisTags tag) {

        Elements element = doc.getElementsByTag(tag.name());
        Elements tempElement = element.clone();
        //Remove any nested
        deleteNestedTags(tempElement, tag);
        String data = tempElement.toString();
        //Remove any instance of "\n"
        if (data.contains("\n"))
            data = data.replaceAll("\n", "").trim();
        //Remove start and end tags
        if (data.contains(("<" + tag.name() + ">").toLowerCase()))
            data = data.replaceAll("<" + tag.name().toLowerCase() + ">", "").trim();
        if (data.contains(("</" + tag.name() + ">").toLowerCase()))
            data = data.replaceAll("</" + tag.name().toLowerCase() + ">", "").trim();

        data = data.trim().replaceAll(" +", " ");
        return data;
    }

    private static void deleteNestedTags(Elements element, FbisTags currTag) {

        for (FbisTags tag : FbisTags.values()) {
            if (element.toString().contains("<" + tag.name().toLowerCase() + ">") &&
                    element.toString().contains("</" + tag.name().toLowerCase() + ">") && !tag.equals(currTag)) {
                element.select(tag.toString()).remove();
            }
        }
    }

    private static Document createFBISDocument(FbisTagsData fbisData, IndexWriter iwr) {
        Document document = new Document();
        document.add(new StringField("id", fbisData.getDocNum(), Field.Store.YES));
        document.add(new TextField("title", fbisData.getTi(), Field.Store.YES));
        document.add(new TextField("text", fbisData.getAll(), Field.Store.YES));
        try {
			iwr.addDocument(document);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return document;
    }

    private static String readAFile() throws IOException {
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

    public static void main(String[] args) throws IOException {
        FbisIndexer fbisIndexer = new FbisIndexer("index");
    }
}
