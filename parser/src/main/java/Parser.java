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

import java.io.IOException;
import java.nio.file.Paths;

import static constants.DirectoryConstants.INDEX_DIR;

public class Parser {
  final Analyzer analyzer;
  final Similarity similarity;

  public Parser(Analyzer analyzer, Similarity similarity) {
    this.analyzer = analyzer;
    this.similarity = similarity;
  }

  public void parseAndIndex() {

    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    config.setRAMBufferSizeMB(1024);
    config.setSimilarity(similarity);

    try (Directory directory = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexWriter iwriter = new IndexWriter(directory, config)) {

      LatimesDocumentParser latimesDocumentParser = new LatimesDocumentParser(iwriter);
      latimesDocumentParser.parseAndIndexDocs();

      FTDocumentParser parser = new FTDocumentParser(iwriter);
      parser.parseAndIndexDocs();

      Fr94Parser fr94Parser = new Fr94Parser(iwriter);
      fr94Parser.parseAndIndexDocs();

      FbisParser fbisParser = new FbisParser(iwriter);
      fbisParser.parseAndIndexDocs();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
