import java.io.IOException;

import constants.AnalyzerType;
import documentparsers.DocumentParser;
import documentparsers.FTDocumentParser;

public class Parser {
	public static void main(String args[]) {
		String path = "D:\\TCD\\Sem1\\Information Retreival\\Assignment2data\\Assignment Two\\";
		long startTime = System.currentTimeMillis();
		try {
			startTime = System.currentTimeMillis();
			DocumentParser parser = new FTDocumentParser(AnalyzerType.ENGLISH, path);
			parser.parse();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			System.out.println(
					"Total Time in seconds ====== " + String.valueOf((System.currentTimeMillis() - startTime) / 1000));
		}
	}

}
