import documentparsers.FTDocumentParser;
import documentparsers.FbisParser;
import documentparsers.Fr94Parser;
import documentparsers.LatimesDocumentParser;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class Parser {
  final String INDEX_DIR = "data/index";
  final String DATA_DIR = "data/data";
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
      latimesDocumentParser.parseAndIndexDocs();

      FTDocumentParser parser = new FTDocumentParser(iwriter, DATA_DIR);
      parser.parseAndIndexDocs();

      Fr94Parser fr94Parser = new Fr94Parser(iwriter, DATA_DIR);
      fr94Parser.parseAndIndexDocs();

      FbisParser fbisParser = new FbisParser(iwriter, DATA_DIR);
      fbisParser.parseAndIndexDocs();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
