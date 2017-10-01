package com.smc.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;

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

  public GenWordFile() throws FileNotFoundException {
    this.ds = MongoHelper.getDataStore();
    this.writer = new PrintWriter("words.txt");
  }

  public void run() {
    // Get the set of words that are used as hashtags and keywords
    HashSet<StringCountResult> words = new HashSet<>();
    ds.createQuery(HashTag.class).forEach(words::add);
    ds.createQuery(KeyWord.class).forEach(words::add);

    // Sort the words in to descending order of count
    words.stream().sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
        // Print them to the file
        .forEach((scr) -> writer.println(scr.getString()));

    // Close the writer
    writer.close();
  }

  public static void main(String[] args) throws FileNotFoundException {
    new GenWordFile().run();
  }

}
