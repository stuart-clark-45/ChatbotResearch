package com.smc;

import static org.mongodb.morphia.aggregation.Group.grouping;
import static org.mongodb.morphia.query.Sort.descending;

import java.util.HashMap;
import java.util.Iterator;

import com.mongodb.AggregationOptions;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.aggregation.Accumulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.candmcomputing.util.MongoHelper;
import com.mongodb.DBCollection;
import com.smc.model.HashTag;
import com.smc.model.KeyPhrase;
import com.smc.model.KeyWord;
import com.smc.model.ParsedTweet;
import com.smc.model.StringCountResult;

/**
 * Used to perform analysis on {@link ParsedTweet}s.
 *
 * @author Stuart Clark
 */
public class Analysis {

  private static final Logger LOGGER = LoggerFactory.getLogger(Analysis.class);

  private static final String COUNT = "count";

  private final Accumulator counter;
  private final AggregationOptions options;

  private Datastore ds;

  public Analysis() {
    this.ds = MongoHelper.getDataStore();
    this.counter = new Accumulator("$sum", 1);
    this.options = AggregationOptions.builder().allowDiskUse(true).build();
  }

  private void run() {
    LOGGER.info("Running Analysis...");

    // Count the number of times that each hashtags appears and write the result to the database
    LOGGER.info("Analysing hashtags...");
    countDistinctElements("hashtags", HashTag.class);

    // Count the number of times that each keyword appears and write the results to the database
    LOGGER.info("Analysing keywords...");
    countDistinctElements("keywords", KeyWord.class);

    // Count the number of times that each phrase appears and write the results to the database
    LOGGER.info("Analysing keyphrases...");
    countDistinctElements("keyphrases", KeyPhrase.class);

    LOGGER.info("Finished running Analysis...");
  }

  /**
   * Looks at all of the lists / sets which are assigned to {@code field} and counts the total
   * number of times that each distinct element appears. Results are written to the database.
   *
   * @param field a field which has a list / set as a value.
   * @return a {@link Iterator<HashMap>} sorted in descending order according to count.
   */
  private <T extends StringCountResult> void countDistinctElements(String field, Class<T> clazz) {
    // Drop the collection
    DBCollection collection = ds.getCollection(clazz);
    collection.drop();


    // Perform aggregation and store result in database. Indexes are dropped before inserting into
    // collection takes place to reduce aggregation time.
    collection.dropIndexes();
    ds.createAggregation(ParsedTweet.class).unwind(field).group(field, grouping(COUNT, counter))
        .sort(descending(COUNT)).out(clazz, options);
    ds.ensureIndexes(clazz);
  }

  public static void main(String[] args) {
    new Analysis().run();
  }

}
