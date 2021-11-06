package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.lucene.analysis.CharArraySet;

/**
 * This class is use to provide a set of custom stop words. This is a singleton
 * class.
 * 
 * @author chait
 *
 */
public class StopWordGenerator {

	private static final String STOP_WORDS_FILE = "EnglishStopWords.txt";

	private static ArrayList<String> customStopwords = new ArrayList<>();

	private CharArraySet charset;

	private StopWordGenerator() {
		if (charset == null) {
			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(StopWordGenerator.class.getClassLoader().getResourceAsStream(STOP_WORDS_FILE)))) {
				String word;
				while ((word = br.readLine()) != null) {
					customStopwords.add(word);
				}
				charset = new CharArraySet(customStopwords, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
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
