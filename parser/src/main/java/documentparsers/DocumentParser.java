package documentparsers;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import constants.AnalyzerType;
import utils.StopWordGenerator;


public abstract class DocumentParser {
	
	private static final String THIS_CLASS_NAME = DocumentParser.class.getName();

	private String INDEX_DIRECTORY;
	
	protected static String DATA_DIRECTORY;

	private static final Logger logger = Logger.getLogger(THIS_CLASS_NAME);

	private Analyzer analyzer;

	private AnalyzerType analyzerType;

	private static StopWordGenerator stopWordGenerator = StopWordGenerator.getInstance();
	
	protected static IndexWriter writer;
	
	private static Directory directory;
	
	

	public DocumentParser() {
	}


	public DocumentParser(AnalyzerType analyzerType,String path) throws IOException {
		DATA_DIRECTORY = path;
		this.analyzerType = analyzerType;
		if (AnalyzerType.STANDARD.equals(this.analyzerType)) {
			this.analyzer = new StandardAnalyzer();
			this.analyzer.setVersion(Version.LUCENE_8_10_0);
			this.INDEX_DIRECTORY = "../index/bm25/standard";
		} else if (AnalyzerType.ENGLISH.equals(this.analyzerType)) {
			this.analyzer = new EnglishAnalyzer(stopWordGenerator.getCharset());
			this.analyzer.setVersion(Version.LUCENE_8_10_0);
			this.INDEX_DIRECTORY = "../index/bm25/english";
		} 
		if(directory == null) {
			directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
		}
		if(writer == null) {
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			writer = new IndexWriter(directory, config);
		}
	}
	
	
	public abstract void parse() throws IOException;
	
	protected void closeWriter() throws IOException {
		writer.close();
	}
	
	protected void closeDirectory() throws IOException {
		directory.close();
	}


}
