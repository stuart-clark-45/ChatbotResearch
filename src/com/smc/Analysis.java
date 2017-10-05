package com.smc;

import static com.candmcomputing.model.Mode.Value.TEST;
import static org.mongodb.morphia.aggregation.Group.grouping;
import static org.mongodb.morphia.query.Sort.descending;

import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.aggregation.Accumulator;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.candmcomputing.model.Mode;
import com.candmcomputing.util.ConfigHelper;
import com.candmcomputing.util.MongoHelper;
import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.font.FontWeight;
import com.kennycason.kumo.font.KumoFont;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.palette.ColorPalette;
import com.mongodb.AggregationOptions;
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

  private static final boolean IGNORE_RT = ConfigHelper.getBoolean("ignoreRetweets");

  private static final Mode.Value MODE = ConfigHelper.getMode();

  private static final ExecutorService ES =
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

  private final Accumulator counter;
  private final AggregationOptions options;

  private Datastore ds;

  public Analysis() {
    this.ds = MongoHelper.getDataStore();
    this.counter = new Accumulator("$sum", 1);
    this.options = AggregationOptions.builder().allowDiskUse(true).build();
  }

  public void run() {
    LOGGER.info("Running Analysis...");

    // Count the number of times that each hashtags appears and write the result to the database
    LOGGER.info("Analysing hashtags...");
    countDistinctElements("hashtags", HashTag.class);
    wordCloud(HashTag.class, "hashtags.png", 200);

    // Count the number of times that each keyword appears and write the results to the database
    LOGGER.info("Analysing keywords...");
    countDistinctElements("keywords", KeyWord.class);
    wordCloud(KeyWord.class, "keywords.png", 200);

    // Count the number of times that each phrase appears and write the results to the database
    LOGGER.info("Analysing keyphrases...");
    countDistinctElements("keyphrases", KeyPhrase.class);
    wordCloud(KeyPhrase.class, "keyphrases.png", 100);

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

    // Decided whether retweets should be included in results
    Query<ParsedTweet> match = ds.createQuery(ParsedTweet.class);
    if (IGNORE_RT) {
      match.field("retweet").equal(false);
    }

    // Perform aggregation and store result in database. Indexes are dropped before inserting into
    // collection takes place to reduce aggregation time.
    collection.dropIndexes();
    ds.createAggregation(ParsedTweet.class).match(match).unwind(field)
        .group(field, grouping(COUNT, counter)).sort(descending(COUNT)).out(clazz, options);
    setRanking(clazz);
    ds.ensureIndexes(clazz);
  }

  /**
   * Concurrently set the rank for each of the aggregation results
   * 
   * @param clazz the {@link Class} of the model used to store the aggregation results.
   * @param <T>
   */
  private <T extends StringCountResult> void setRanking(Class<T> clazz) {

    // Create the futures
    int counter = 1;
    List<Future<?>> futures = new LinkedList<>();
    // This works as results are returned in rank order
    for (T result : ds.createQuery(clazz)) {
      final int rank = counter++;
      futures.add(ES.submit(() -> {
        result.setRank(rank);
        ds.save(result);
      }));
    }

    // Wait for all the futures
    for (Future<?> future : futures) {
      try {
        future.get();
      } catch (InterruptedException | ExecutionException e) {
        LOGGER.error("Failed to set ranking", e);
      }
    }

  }

  private <T extends StringCountResult> void wordCloud(Class<T> clazz, String file,
      int wordsInCloud) {
    // Load the word frequencies from the database
    FindOptions limit = new FindOptions().limit(wordsInCloud);
    List<WordFrequency> frequencies = ds.createQuery(clazz).order(descending(COUNT)).asList(limit)
        .stream()
        .map(scr -> new WordFrequency(scr.getString().replaceAll("\\s", "-"), (int) scr.getCount()))
        .collect(Collectors.toList());

    // Create the word cloud
    Dimension dimension = new Dimension(800, 500);
    WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
    wordCloud.setPadding(2);
    wordCloud.setColorPalette(new ColorPalette(new Color(0x4055F1), new Color(0x408DF1),
        new Color(0x40AAF1), new Color(0x40C5F1), new Color(0x40D3F1), new Color(0xFFFFFF)));
    wordCloud.setFontScalar(new LinearFontScalar(10, 40));
    wordCloud.setKumoFont(new KumoFont("LICENSE PLATE", FontWeight.BOLD));
    wordCloud.build(frequencies);

    // Write it to the file if not in test mode
    if (MODE != TEST) {
      wordCloud.writeToFile(file);
    }
  }

  public static void main(String[] args) {
    new Analysis().run();
  }

}
