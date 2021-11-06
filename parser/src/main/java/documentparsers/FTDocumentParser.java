package documentparsers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import constants.AnalyzerType;
import constants.CranFieldType;

public class FTDocumentParser extends DocumentParser {
	
	private static final String PATH = "ft";
	private static final String PLACEHOLDER = "";
	private Document document;
	private Field idField;
	private Field titleField;
	private Field bodyField;
	private static List<Document> documentList = new ArrayList<>();
	
	public FTDocumentParser() {
		super();
		document = new Document();
		idField = new TextField("id", PLACEHOLDER, Field.Store.YES);
		titleField = new Field("title", PLACEHOLDER, FTDocumentParser.getField(CranFieldType.TITLE));
		bodyField = new Field("content", PLACEHOLDER, FTDocumentParser.getField(CranFieldType.BODY));
	}


	public FTDocumentParser(AnalyzerType analyzerType, String path) throws IOException {
		super(analyzerType, path);
		document = new Document();
		idField = new TextField("id", PLACEHOLDER, Field.Store.YES);
		titleField = new Field("title", PLACEHOLDER, FTDocumentParser.getField(CranFieldType.TITLE));
		bodyField = new Field("content", PLACEHOLDER, FTDocumentParser.getField(CranFieldType.BODY));
	}


	@Override
	public void parse() throws IOException {
		Path rootdir = Paths.get(DATA_DIRECTORY+PATH);
		List<Path> directories = null;
		try (Stream<Path> f = Files.walk(rootdir)) {
			directories = f.filter(file -> (Files.isDirectory(file) && !rootdir.equals(file))).collect(Collectors.toList());
		}
		for(Path dir : directories) {
			try (Stream<Path> f = Files.walk(dir)) {
				f.filter(Files::isRegularFile).forEach(this::parseFile);
				System.out.println("Done parsing");
			}
			catch(Exception e) {
				System.out.println("Error --");
				e.printStackTrace();
			}
		}
		writer.addDocuments(documentList);
			closeWriter();
			closeDirectory();
	}
	
	
	public static FieldType getField(CranFieldType type) {
		FieldType ft = null;
		switch (type) {
		case TITLE:
			ft = new FieldType();
			ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
			ft.setStored(true);
			ft.setTokenized(true);
			ft.setStoreTermVectors(true);
			break;
		case BODY:
			ft = new FieldType();
			ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
			ft.setStored(true);
			ft.setTokenized(true);
			ft.setStoreTermVectors(true);
		default:
			break;
		}
		return ft;
	}
	
	public void parseFile(Path p) {
		Document document  = null;
		if (Files.isDirectory(p)) {
			System.out.println(p.getFileName());
		} else {
			try {
				System.out.println("Parsing Documents at path - "+p.getFileName().toString());
				try (Stream<String> m = Files.lines(p)) {
					StringBuffer bf = new StringBuffer();
					m.forEach(s -> bf.append(s).append("\n"));
					org.jsoup.nodes.Document d = Jsoup.parse(bf.toString());
					Elements doc = d.getElementsByTag("doc");
					for (Element e : doc) {
						//System.out.println(e.getElementsByTag("docno").text());
						//System.out.println(e.getElementsByTag("headline").text());
						String title = e.getElementsByTag("headline").text();
						String body = e.getElementsByTag("text").text();
						String id = e.getElementsByTag("docno").text();
						document = new Document();
						document.add(new TextField("id", id, Field.Store.YES));
						document.add(new Field("title", title, FTDocumentParser.getField(CranFieldType.TITLE)));
						document.add(new Field("content", body, FTDocumentParser.getField(CranFieldType.BODY)));
						documentList.add(document);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
	}

}
