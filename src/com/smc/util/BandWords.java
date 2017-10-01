package com.smc.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Used to load words from bad words file into a set and provided access to it.
 *
 * @author Stuart Clark
 */
public class BandWords {

  private static Set<String> words;

  private final String file;


  public BandWords() {
    this("conf/band-words.txt");
  }

  /**
   * @param file the file to load the band words from.
   */
  public BandWords(String file) {
    this.file = file;
  }

  /**
   * @param word
   * @return true if {@code word} is banned, false otherwise.
   * @throws CbrException
   */
  public boolean isBanned(String word) throws CbrException {
    return getWords().contains(prepareWord(word));
  }

  /**
   * @return a set of words which are band from being hashtags and keywords.
   * @throws CbrException
   */
  private Set<String> getWords() throws CbrException {
    if (words == null) {

      words = new HashSet<>();

      // Read the bad words file
      try (Stream<String> stream = Files.lines(Paths.get(file))) {

        // Add the words from the file into the set
        stream.forEach((word) -> {
          word = prepareWord(word);
          if (!word.startsWith("#") && !word.isEmpty()) {
            words.add(word);
          }
        });

      } catch (IOException e) {
        throw new CbrException("Failed to get band words", e);
      }
    }

    return words;
  }

  private String prepareWord(String word) {
    return word.trim().toUpperCase();
  }

}
