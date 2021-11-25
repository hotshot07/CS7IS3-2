package utils;

import org.apache.lucene.analysis.CharArraySet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class is use to provide a set of custom stop words. This is a singleton class.
 *
 * @author chait
 */
public class StopWordGenerator {

  private static final String STOP_WORDS_FILE = "EnglishStopWords.txt";

  private static final Set<String> customStopwordsSet = new HashSet<>();

  private CharArraySet charset;

  private StopWordGenerator() {
    try (BufferedReader br =
        new BufferedReader(
            new InputStreamReader(
                Objects.requireNonNull(
                    StopWordGenerator.class
                        .getClassLoader()
                        .getResourceAsStream(STOP_WORDS_FILE))))) {
      String word;
      while ((word = br.readLine()) != null) {
        customStopwordsSet.add(word);
      }
      charset = new CharArraySet(new ArrayList<>(customStopwordsSet), true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static class SingletonHolder {
    public static final StopWordGenerator parser = new StopWordGenerator();
  }

  public static StopWordGenerator getInstance() {
    return SingletonHolder.parser;
  }

  public CharArraySet getCharset() {
    return charset;
  }
}
