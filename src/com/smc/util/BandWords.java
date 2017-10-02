package com.smc.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Used to load words from bad words file into a set and provided access to a singleton instance.
 *
 * @author Stuart Clark
 */
public class BandWords {

  private static BandWords instance;

  private Set<String> words;

  private BandWords() throws CbrException {
    this("conf/band-words.txt");
  }

  /**
   * This method is intended for use when unit testing.
   *
   * @param file the file to load the band words from.
   */
  /* package */ BandWords(String file) throws CbrException {
    this.words = new HashSet<>();

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

  /**
   * @param word
   * @return true if {@code word} is banned, false otherwise.
   * @throws CbrException
   */
  public boolean isBanned(String word) {
    return words.contains(prepareWord(word));
  }

  private String prepareWord(String word) {
    return word.trim().toUpperCase();
  }

  public static BandWords getInstance() throws CbrException {
    if (instance == null) {
      instance = new BandWords();
    }
    return instance;
  }

}
