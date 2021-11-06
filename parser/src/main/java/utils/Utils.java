package utils;

import java.util.Locale;

public class Utils {

  public static String replacePunctuation(String inputString) {
    return inputString.toLowerCase(Locale.ROOT).replaceAll("\\p{Punct}", " ");
  }
}
