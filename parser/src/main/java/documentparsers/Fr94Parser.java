package documentparsers;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;


import static constants.DirectoryConstants.DATA_DIR;

import static utils.CommonUtils.replacePunctuation;

public class Fr94Parser {

    public static ArrayList<File> files = new ArrayList<File>();
    public static ArrayList<org.apache.lucene.document.Document> docDatas =
            new ArrayList<org.apache.lucene.document.Document>();

    private final String FR_DIR_PATH = DATA_DIR + "/fr94";
    private final IndexWriter iwriter;
    private final HashMap<String, Integer> ALL_ID = new HashMap<String, Integer>();

    public Fr94Parser(IndexWriter indexWriter) {
        this.iwriter = indexWriter;

    }

    public void parseAndIndexDocs() throws IOException {
        long start_time = System.currentTimeMillis();
        this.findAllFiles(this.FR_DIR_PATH);
        for (File file : files) {
            this.parseContent(file);
        }

        this.iwriter.addDocuments(docDatas);
        System.out.format(
                "Indexed %s documents in %s seconds\n",
                files.size(), TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start_time));
    }

    private void parseContent(File file) throws IOException {

        HashMap<String, String> result = new HashMap<String, String>();
        org.jsoup.nodes.Document corpus = Jsoup.parse(file, "UTF-8", " ");
        Elements docs = corpus.getElementsByTag("DOC");

        for (Element doc : docs) {

            org.apache.lucene.document.Document document = new org.apache.lucene.document.Document();
            String textContent = "";
            String date = "";
            String title = "";
            String docId = "";

            // extract doc id
            Elements docIdField = doc.getElementsByTag("PARENT");
            if (docIdField.size() != 0) {
                docId = docIdField.get(0).text();
                if (ALL_ID.containsKey(docId)){
                    ALL_ID.put(docId, ALL_ID.get(docId) + 1);
                }
                else{
                    ALL_ID.put(docId, 0);}

            } else {
                System.out.println("Missing doc id");
            }
            Element docBody = doc.getElementsByTag("text").get(0);
            // extract doc title
            String[] textFields = docBody.toString().split("\n");
            Elements titleFields = docBody.getElementsByTag("DOCTITLE");
            if (titleFields.size() != 0) {
                title = titleFields.get(0).text();
            }
            // extract doc date
            Elements dateField = docBody.getElementsByTag("DATE");
            if (dateField.size() != 0) {
                date = dateField.get(0).text();
            }

            // building text
            String updated_doc_id = docId + ALL_ID.get(docId).toString();
            textContent = docBody.text();
            document.add(new StringField("docno", updated_doc_id, Field.Store.YES));
            document.add(new TextField("title", replacePunctuation(title.strip()), Field.Store.YES));
            document.add(
                    new TextField("content", replacePunctuation(textContent.strip()), Field.Store.YES));
            docDatas.add(document);
        }
    }

    private void findAllFiles(String in) {
        File dir = new File(in);
        File[] directoryListing = dir.listFiles();

        if (directoryListing != null) {
            for (File child : directoryListing) {
                // Do something with child
                if (child.isFile() && child.getName().matches("^fr.*")) {
                    files.add(child);
                    // cleanText(child);
                } else if (child.isDirectory()) {
                    findAllFiles(child.getAbsolutePath());
                }
            }
        } else {
            System.out.println(dir.getAbsoluteFile());
        }
    }
}