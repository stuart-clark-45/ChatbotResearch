package com.smc;

import static org.mongodb.morphia.aggregation.Group.grouping;
import static org.mongodb.morphia.query.Sort.descending;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.aggregation.Accumulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.candmcomputing.util.MongoHelper;
import com.smc.model.ParsedTweet;

public class Analysis {

  private static final Logger LOGGER = LoggerFactory.getLogger(Analysis.class);

  private static final String ELEMENT = "_id";
  private static final String COUNT = "count";

  private final Accumulator counter;

  private Datastore ds;

  public Analysis() {
    this.ds = MongoHelper.getDataStore();
    this.counter = new Accumulator("$sum", 1);
  }

  private void run() {
    // Count the number of times that each hashtags appears and write the result to a file
    writeResults(countDistinctElements("hashtags"), "hashtags.csv");

    // Count the number of times that each keyword appears and write the results to a file
    writeResults(countDistinctElements("keywords"), "keywords.csv");

    // Count the number of times that each phrase appears and write the results to a file
    writeResults(countDistinctElements("keyphrases"), "keyphrases.csv");
  }

  /**
   * Looks at all of the lists / sets which are assigned to {@code field} and counts the total
   * number of times that each distinct element appears.
   *
   * @param field a field which has a list / set as a value.
   * @return a {@link Iterator<HashMap>} sorted in descending order according to count. Each
   *         {@link HashMap} in the {@link Iterator} has two keys:
   *
   *         "_id" => the distinct element that has been counted
   *
   *         COUNT => the number of times that the distinct element appeared in the database.
   */
  private Iterator<HashMap> countDistinctElements(String field) {
    return ds.createAggregation(ParsedTweet.class).unwind(field)
        .group(field, grouping(COUNT, counter)).sort(descending(COUNT)).aggregate(HashMap.class);
  }

  /**
   * Writes the results to a csv file with the form: ELEMENT,COUNT
   *
   * @param results the results returned by calling {@link Analysis#countDistinctElements(String)}.
   * @param filename the name of the file to save the results to.
   */
  private void writeResults(Iterator<HashMap> results, String filename) {
    try {
      // Write the results to a file
      PrintWriter writer = new PrintWriter(filename, "UTF-8");
      results.forEachRemaining(result -> {
        writer.println(result.get(ELEMENT) + "," + result.get(COUNT));
      });
      writer.close();

    } catch (FileNotFoundException | UnsupportedEncodingException e) {
      LOGGER.error("Failed to write results to " + results, e);
    }

  }

  public static void main(String[] args) {
    new Analysis().run();
  }

}
