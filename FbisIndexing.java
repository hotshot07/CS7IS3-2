package ie.tcd.singhr3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
 
public class FbisIndexing
{
	
	public void indexing() {
		
		//Index Files Based on different Analyzer (English & Standard)
		String indexDirectory = "index";
//		String indexDirectoryStandard = "indexStandard";
		
		
//		if (!Files.isReadable(cranDir)) {
// 	           System.out.println("Path doesn\'t exists");
// 	           System.exit(1);
// 	       }
		
		//passing "cran/cran.all.1400" as argument
//		if(arg==null || arg=="") {
//			System.out.println("Path doesn\'t exists");
//			System.exit(1);
//		}
//		String cranPath = arg;
//		final Path cranDir = Paths.get(cranPath);
	
		//code for using English Analyzer for indexing
        try {
            System.out.println("Indexing it to'" + indexDirectory + "'...");
		
		//Path to index directory for English Analyzer
            Directory dir = FSDirectory.open(Paths.get(indexDirectory ));
            Analyzer analyzer = new EnglishAnalyzer();
            IndexWriterConfig iwconfig = new IndexWriterConfig(analyzer);

            iwconfig.setOpenMode(OpenMode.CREATE);

            IndexWriter iw = new IndexWriter(dir, iwconfig);
            
//            File folder = new File("Assignment Two/fbis");
//            File[] listOfFiles = folder.listFiles();
//
//            for (File file : listOfFiles) {
//                if (file.isFile()) {
//                    System.out.println(file.getName());
//                }
//            }
            
            File folder = new File("Assignment Two/fbis");
            File[] listOfFiles = folder.listFiles();

            for (File file : listOfFiles) {
                if (file.isFile()) {
                    System.out.println(file.getName());
            		final Path cranDir = Paths.get("Assignment Two/fbis/" + file.getName());
                    cran1400(iw, cranDir);
                }
            }

            iw.forceMerge(1);

            iw.close();

        } catch (IOException e) {
            System.out.println(e);
        }
        
		//code for using Standard Analyzer for Indexing
//        try {
//            System.out.println("Indexing it to'" + indexDirectoryStandard + "'...");
//
//            Directory dir = FSDirectory.open(Paths.get(indexDirectoryStandard));
//          
//            Analyzer analyzer = new StandardAnalyzer();
//            IndexWriterConfig iwconfig = new IndexWriterConfig(analyzer);
//
//            iwconfig.setOpenMode(OpenMode.CREATE);
//
//            IndexWriter iw = new IndexWriter(dir, iwconfig);
//		
//		//Method call for splitting the documents
//            cran1400(iw, cranDir);
//
//            iw.forceMerge(1);
//
//            iw.close();
//
//        } catch (IOException e) {
//            System.out.println(e);
//        }
    }

    //splitting cran.all.1400 documents into id, title, author, work separately
    static void cran1400(IndexWriter iw, Path file) throws IOException {
        try (InputStream is = Files.newInputStream(file)) {

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            System.out.println("Start Indexing.");

            String strLine = br.readLine();
            String str2 = "";

            while(strLine != null){
                Document doc = new Document();
                if(strLine.startsWith("<DOCNO>")){
                    strLine = strLine.trim();
                    doc.add(new StringField("id", strLine.substring(7,14), Field.Store.YES));
                    strLine = br.readLine();
                }
                if(strLine.startsWith("<HT>")){
                    strLine = strLine.trim();
                    doc.add(new StringField("HT", strLine.substring(5,21), Field.Store.YES));
                    strLine = br.readLine();
                }
                if (strLine.startsWith("<HEADER>")){
                	strLine = br.readLine();
                    while(!strLine.startsWith("</HEADER")){
                    	str2 += strLine + " ";
                        strLine = br.readLine();
                    }
                    doc.add(new TextField("header", str2, Field.Store.YES));
                    str2 = "";
                }
                if (strLine.startsWith("<TEXT>")){
                	strLine = br.readLine();
                    while(!strLine.startsWith("</TEXT>")){
                    	str2 += strLine + " ";
                        strLine = br.readLine();
                    }
                    doc.add(new TextField("text", str2, Field.Store.YES));
                    str2 = "";
                }
//                if (strLine.startsWith(".B")){
//                	strLine = br.readLine();
//                    while(!strLine.startsWith(".W")){
//                    	str2 += strLine + " ";
//                        strLine = br.readLine();
//                    }
//                    doc.add(new TextField("bibliography", str2, Field.Store.YES));
//                    str2 = "";
//                }
//                if (strLine.startsWith(".W")){
//                	strLine = br.readLine();
//                    while(strLine != null && !strLine.startsWith(".I")){
//                    	str2 += strLine + " ";
//                        strLine = br.readLine();
//                    }
//                    doc.add(new TextField("work", str2, Field.Store.YES));
//                    str2 = "";
//                }
                iw.addDocument(doc);
            }
        }
    }
}