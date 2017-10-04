package com.smc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.mongodb.morphia.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.candmcomputing.util.IterationLogger;
import com.candmcomputing.util.MongoHelper;
import com.mongodb.DBCollection;
import com.smc.model.ParsedTweet;
import com.smc.model.Token;
import com.smc.model.Tweet;
import com.smc.util.BandWords;
import com.smc.util.CbrException;
import com.smc.util.Combinations;
import com.smc.util.Parser;

/**
 * Used to take {@link Tweet}s and parse them into {@link ParsedTweet}s.
 *
 * @author Stuart Clark
 */
public class TweetParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(TweetParser.class);

  private static final String RETWEET = "RT";

  private static final int LOG_INTERVAL = 100;

  private final Datastore ds;
  private final ExecutorService es;
  private final BandWords bandwords;

  public TweetParser() throws CbrException {
    this.ds = MongoHelper.getDataStore();

    LOGGER.info("Initialising StanfordCoreNLP...");
    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");

    int nThreads = Runtime.getRuntime().availableProcessors() * 2;
    this.es = Executors.newFixedThreadPool(nThreads);

    this.bandwords = BandWords.getInstance();
  }

  public void run() {
    // Log how many tweets there are
    int numTweet = (int) ds.getCount(Tweet.class);
    LOGGER.info("Parsing " + numTweet + " tweets...");

    // Drop existing parsed tweets and indexes. Dropping indexes reduces insert time.
    DBCollection collection = ds.getCollection(ParsedTweet.class);
    collection.drop();
    collection.dropIndexes();

    // Iterate over all of the imported tweets and parse them in parallel.
    IterationLogger itLogger = IterationLogger.builder().logger(LOGGER).logInterval(LOG_INTERVAL)
        .maxIterations(numTweet).task("Futures created").build();
    List<Future<?>> futures = new ArrayList<>(numTweet);
    for (Tweet tweet : ds.createQuery(Tweet.class)) {
      futures.add(es.submit(() -> parseTweet(tweet)));
      itLogger.iteration();
    }

    // Get all of the futures
    itLogger = IterationLogger.builder().logger(LOGGER).logInterval(LOG_INTERVAL)
        .maxIterations(numTweet).task("tweets parsed").build();
    for (Future<?> future : futures) {
      try {
        future.get();
        itLogger.iteration();
      } catch (InterruptedException | ExecutionException e) {
        LOGGER.error("Failed to get future", e);
      }
    }

    // Reapply indexes
    LOGGER.info("Ensuring indexes...");
    ds.ensureIndexes(ParsedTweet.class);

    LOGGER.info("Finished parsing tweets.");
  }

  /**
   * Takes {@code tweet} and parses it into a {@link ParsedTweet} which is then stored in the db.
   *
   * @param tweet
   */
  private void parseTweet(Tweet tweet) {
    // The parsed tweet
    ParsedTweet parsed = new ParsedTweet();

    // Set the tweet id
    parsed.setTweetId(tweet.getTweetId());

    // Set unparsed
    String text = tweet.getText();
    parsed.setUnparsed(text);

    // Filter out the bad hashtags
    Set<String> hashtags = tweet.getHashtags().stream().filter(ht -> !bandwords.isBanned(ht))
        .map(String::toUpperCase).collect(Collectors.toSet());
    parsed.setHashtags(hashtags);

    // Parse the text to obtain tokens with POS and NER tags
    List<Token> tokens = new Parser(text).getTokens();
    parsed.setTokens(tokens);

    // Set retweet
    parsed.setRetweet(tokens.get(0).getText().equals(RETWEET));

    // Select key words
    Set<String> keywords = tokens.stream().filter(this::isKeyWord).map(Token::getText)
        .map(String::toUpperCase).collect(Collectors.toSet());
    parsed.setKeywords(keywords);

    // Combine keywords and hashtags
    HashSet<String> kwAndHt = new HashSet<>(keywords);
    kwAndHt.addAll(hashtags);

    // Generate the key phrases
    Set<String> keyphrases = new Combinations<>(kwAndHt, 2, 4).find().stream()
        .map(this::buildPhrase).collect(Collectors.toSet());
    parsed.setKeyphrases(keyphrases);

    // Save the parsed tweet
    ds.save(parsed);
  }

  /**
   * @param words
   * @return an String with each of the words in {@code words} in alphabetical order separated by a
   *         space. Having the phrases in alphabetical make aggregation easier during
   *         {@link Analysis}.
   */
  private String buildPhrase(Set<String> words) {
    List<String> wordsList = new ArrayList<>(words);
    Collections.sort(wordsList);
    return String.join(" ", wordsList);
  }

  private boolean isKeyWord(Token t) {
    String lower = t.getText().toLowerCase();
    String pos = t.getPos();

    // Can be a noun or a verb
    if (!pos.startsWith("NN") && !pos.startsWith("VB"))
      return false;

    // URLs not allowed
    if (lower.startsWith("https://") || lower.startsWith("http://"))
      return false;

    // Hashtags not allowed
    if (lower.startsWith("#"))
      return false;

    // The word cannot be a band word
    return !bandwords.isBanned(lower);
  }

  public static void main(String[] args) throws CbrException {
    new TweetParser().run();
  }

}

