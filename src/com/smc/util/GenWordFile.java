package com.smc.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.mongodb.morphia.Datastore;

import com.candmcomputing.util.MongoHelper;
import com.smc.model.HashTag;
import com.smc.model.KeyWord;
import com.smc.model.StringCountResult;

/**
 * Used to generate a file with all the distinct words that are used as hashtags and keywords. The
 * words in the file are sorted from most frequently occurring to least frequently occurring. The
 * file that is output can then be used to help create {@code band-words.txt}.
 *
 * @author Stuart Clark
 */
public class GenWordFile {

  private Datastore ds;
  private final PrintWriter writer;
  private final Map<String, Long> words;

  public GenWordFile() throws FileNotFoundException {
    this.ds = MongoHelper.getDataStore();
    this.writer = new PrintWriter("words.txt");
    this.words = new HashMap<>();
  }

  public void run() {
    // Get the set of words that are used as hashtags and keywords
    ds.createQuery(HashTag.class).forEach(this::addToWords);
    ds.createQuery(KeyWord.class).forEach(this::addToWords);

    // Sort the words in to descending order of count
    words.entrySet().stream().sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
        // Print them to the file
        .forEach((scr) -> writer.println(scr.getKey()));

    // Close the writer
    writer.close();
  }

  /**
   * Add {@code scr} to words, if words already contains the string then update the count total.
   *
   * @param scr
   */
  private void addToWords(StringCountResult scr) {
    String key = scr.getString();
    long value = scr.getCount();
    if (words.containsKey(key)) {
      Long count = words.get(key) + value;
      words.put(key, count);
    } else {
      words.put(key, value);
    }
  }

  public static void main(String[] args) throws FileNotFoundException {
    new GenWordFile().run();
  }

}
