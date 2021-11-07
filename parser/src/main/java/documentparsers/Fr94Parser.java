

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;


import org.apache.lucene.benchmark.byTask.feeds.DocData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXParseException;

public class Fr94Parser {
    public static final String PJG_REGEX = "^(<!-- PJG).*";
    public static ArrayList<File> files = new ArrayList<File>();
    public static ArrayList<DocData> docDatas = new ArrayList<DocData>();
    public static final String newPath = "docs/fr94-clean/";
    public static final String oldPath = "docs/fr94/";

    public static void main(String[] args) throws Exception{
        Fr94Parser parser = new Fr94Parser();
//        parser.findAllFiles(newPath);
//        for (File file:files){
//            parser.generateCleanedText(file);
//        }
        files = new ArrayList<File>();
        //480 files in total
        parser.findAllFiles(oldPath);
        for(File file:files){
        parser.parseContent(file);
        }

    }

    public void parseContent(File file) throws IOException{


        HashMap<String,String> result = new HashMap<String,String>();
        Document corpus = Jsoup.parse(file, "UTF-8", " ");
        Elements docs = corpus.getElementsByTag("DOC");

        for (Element doc : docs){

            DocData docData = new DocData();
            StringBuilder textContent = new StringBuilder();
            String date = "";
            String title = "";
            String docId = "";

            //extract doc id
            Elements docIdField = doc.getElementsByTag("DOCNO");
            if (docIdField.size()!=0){
                docId = docIdField.get(0).text();
            }
            else{
                System.out.println("Missing doc id");
            }
            Element docBody = doc.getElementsByTag("text").get(0);
                //extract doc title
                String[] textFields = docBody.toString().split("\n");
                Elements titleFields = docBody.getElementsByTag("DOCTITLE");
                if (titleFields.size()!=0){
                    title = titleFields.get(0).text();
                }
                //extract doc date
                Elements dateField = docBody.getElementsByTag("DATE");
                if (dateField.size()!=0){
                    date = dateField.get(0).text();
                }


                //building text
                if (textFields.length > 2){
                    String content = textFields[1];
                    if (!content.matches("^<.*") && !content.matches("^ <.*")){
                        textContent.append(content);
                    }
                }
                Elements summaryField = docBody.getElementsByTag("SUMMARY");
                if (summaryField.size()!=0){
                    textContent.append(summaryField.get(0).text());
                }
                Elements supplemField = docBody.getElementsByTag("SUPPLEM");
                if (supplemField.size()!=0){
                    textContent.append(supplemField.get(0).text());
                }

                docData.setBody(textContent.toString());
                docData.setTitle(title);
                docData.setDate(date);
                docData.setName(docId);
                docDatas.add(docData);
            System.out.println("docid: " + docId);
            System.out.println("title: " + title);
            System.out.println("date: " + date);
            System.out.println("content: " + textContent.toString());


        }



    }


// clean the txt file: remove  SGML compliant comments all contain the string "-- PJG "
    public void generateCleanedText(File file) throws IOException{
        ArrayList<String> outputs = new ArrayList<String>();
        Path path = Paths.get(file.getAbsolutePath());
        Files.lines(path).forEach(line->{
            if (!line.matches(PJG_REGEX)) {
                outputs.add(line);
            }
            else{
//                System.out.println(line);
            }
        });
        String out = newPath + file.getName();
        Files.write(Paths.get(out),outputs);
    }


    //get all files under one directory
    public  void findAllFiles(String in){
        File dir = new File(in);
        File[] directoryListing = dir.listFiles();
//        for (File sub_dir: directoryListing){
//            if (sub_dir.isDirectory()){
//                File[] fr_files = sub_dir.listFiles();
//                for (File child : fr_files) {
//                    if (child.isFile() && child.getName().matches("^fr.*")){
//                        files.add(child);
//                }
//            }
//        }
        if (directoryListing != null) {
            for (File child : directoryListing) {
                // Do something with child
                if (child.isFile() && child.getName().matches("^fr.*")){
                    files.add(child);
                    //cleanText(child);
                }
                else if (child.isDirectory()){
                    findAllFiles(child.getAbsolutePath());
                }
            }
        }
        else {
            System.out.println(dir.getAbsoluteFile());
        }
    }




}
