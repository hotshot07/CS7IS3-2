import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import documentparsers.FTDocumentParser;
import documentparsers.LatimesDocumentParser;

public class Parser {
	static String INDEX_DIR = "data/index";
	// final String DATA_DIR = "data/";
	final String DATA_DIR = "D:\\TCD\\Sem1\\Information Retreival\\Assignment2data\\Assignment Two\\";
	final Analyzer analyzer;
	final Similarity similarity;

	public Parser(Analyzer analyzer, Similarity similarity) {
		this.analyzer = analyzer;
		this.similarity = similarity;
	}

	public void parse() {
		// Create index and pass iwriter to parsers
		boolean indexDirectory = new File(INDEX_DIR).mkdir();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		config.setRAMBufferSizeMB(1024);
		config.setSimilarity(similarity);
		try (Directory directory = FSDirectory.open(Paths.get(INDEX_DIR));
				IndexWriter iwriter = new IndexWriter(directory, config)) {
			LatimesDocumentParser latimesDocumentParser = new LatimesDocumentParser(iwriter, DATA_DIR);
			latimesDocumentParser.parseDocuments();
			FTDocumentParser parser = new FTDocumentParser(iwriter, DATA_DIR);
			parser.parseDocuments();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// This is how the Parser class can be used in the "querier" module,
	public static void main(String[] args) throws IOException {
		Parser parser = new Parser(new EnglishAnalyzer(), new BM25Similarity());
		parser.parse();
	}
}
