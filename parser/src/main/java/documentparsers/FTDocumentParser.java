package documentparsers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import constants.CranFieldType;

public class FTDocumentParser {

	private static final String PATH = "ft";
	private final String LATIMES_DIR_PATH;
	private final IndexWriter iwriter;

	public FTDocumentParser(IndexWriter iwriter, String lATIMES_DIR_PATH) {
		this.LATIMES_DIR_PATH = lATIMES_DIR_PATH;
		this.iwriter = iwriter;
	}

	public void parseDocuments() throws IOException {
		Path rootdir = Paths.get(LATIMES_DIR_PATH + PATH);
		System.out.println("Indexing Documents at Path = " + rootdir.getFileName());
		List<Path> directories = null;
		try (Stream<Path> f = Files.walk(rootdir)) {
			directories = f.filter(file -> (Files.isDirectory(file) && !rootdir.equals(file)))
					.collect(Collectors.toList());
		}
		for (Path dir : directories) {
			try (Stream<Path> f = Files.walk(dir)) {
				f.filter(Files::isRegularFile).forEach(this::parseFile);
				System.out.println("Done Indexing Docuements at Path = " + rootdir.getFileName());
			} catch (Exception e) {
				System.out.println("Error --");
				e.printStackTrace();
			}
		}
	}

	public static FieldType getField(CranFieldType type) {
		FieldType ft = null;
		switch (type) {
		case TITLE:
			ft = new FieldType();
			ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
			ft.setStored(true);
			ft.setTokenized(true);
			ft.setStoreTermVectors(true);
			break;
		case BODY:
			ft = new FieldType();
			ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
			ft.setStored(true);
			ft.setTokenized(true);
			ft.setStoreTermVectors(true);
		default:
			break;
		}
		return ft;
	}

	public void parseFile(Path p) {
		Document document = null;
		if (Files.isDirectory(p)) {
			System.out.println(p.getFileName());
		} else {
			try {
				System.out.println("Parsing Documents at path - " + p.getFileName().toString());
				try (Stream<String> m = Files.lines(p)) {
					StringBuffer bf = new StringBuffer();
					m.forEach(s -> bf.append(s).append("\n"));
					org.jsoup.nodes.Document d = Jsoup.parse(bf.toString());
					Elements doc = d.getElementsByTag("doc");
					for (Element e : doc) {
						// System.out.println(e.getElementsByTag("docno").text());
						// System.out.println(e.getElementsByTag("headline").text());
						String title = e.getElementsByTag("headline").text();
						String body = e.getElementsByTag("text").text();
						String id = e.getElementsByTag("docno").text();
						document = new Document();
						document.add(new StringField("docno", id, Field.Store.YES));
						document.add(new TextField("title", title, Field.Store.YES));
						document.add(new TextField("content", body, Field.Store.YES));
						iwriter.addDocument(document);
						document = null;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
