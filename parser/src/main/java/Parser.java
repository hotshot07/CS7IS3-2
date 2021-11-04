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
  String INDEX_DIR = "index";
  private final Analyzer analyzer;
  private final Similarity similarity;

  public Parser(Analyzer analyzer, Similarity similarity){
    this.analyzer = analyzer;
    this.similarity = similarity;
  }

  public void parser() throws IOException {
    // Create index and pass iwriter to parsers
    boolean indexDirectory = new File(INDEX_DIR).mkdir();

    Directory directory = FSDirectory.open(Paths.get(INDEX_DIR));
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    config.setRAMBufferSizeMB(512);
    config.setSimilarity(similarity);

    IndexWriter iwriter = new IndexWriter(directory, config);

    // adding all parsers here
    LatimesDocumentParser latimesDocumentParser =  new LatimesDocumentParser(iwriter);
    latimesDocumentParser.parseDocument();

    iwriter.close();
    directory.close();
  }
}
