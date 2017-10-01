package com.smc;

import java.util.ArrayList;
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

import com.candmcomputing.util.ConfigHelper;
import com.candmcomputing.util.MongoHelper;
import com.smc.model.ParsedTweet;
import com.smc.model.Phrase;
import com.smc.model.Token;
import com.smc.model.Tweet;
import com.smc.util.Parser;

/**
 * Used to take {@link Tweet}s and parse them into {@link ParsedTweet}s.
 *
 * @author Stuart Clark
 */
public class TweetParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(TweetParser.class);

  private static final Set<String> BAD_HASHTAGS =
      new HashSet<>(ConfigHelper.getList(String.class, "hashtags.exclude"));

  private static final String CHATBOT_REGEX = "#?(chat)?bots?";

  private final Datastore ds;
  private final ExecutorService es;

  public TweetParser() {
    this.ds = MongoHelper.getDataStore();

    LOGGER.info("Initialising StanfordCoreNLP...");
    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");

    int nThreads = Runtime.getRuntime().availableProcessors() * 2;
    this.es = Executors.newFixedThreadPool(nThreads);
  }

  public void run() {
    // Drop existing parsed tweets
    ds.getCollection(ParsedTweet.class).drop();

    // Log how many tweets there are
    int count = (int) ds.getCount(Tweet.class);
    LOGGER.info("Parsing " + count + " tweets...");

    // Iterate over all of the imported tweets and parse them in parallel.
    List<Future<?>> futures = new ArrayList<>(count);
    for (Tweet tweet : ds.createQuery(Tweet.class)) {
      futures.add(es.submit(() -> parseTweet(tweet)));
    }
    futures.forEach(voidFuture -> {
      try {
        voidFuture.get();
      } catch (InterruptedException | ExecutionException e) {
        LOGGER.error("Failed to get future", e);
      }
    });

  }

  /**
   * Takes {@code tweet} and parses it into a {@link ParsedTweet} which is then stored in the db.
   *
   * @param tweet
   */
  private void parseTweet(Tweet tweet) {
    // The parsed tweet
    ParsedTweet parsed = new ParsedTweet();

    // Set unparsed
    String text = tweet.getText();
    parsed.setUnparsed(text);

    // Remove all hashtags
    text = text.replaceAll("#", "");

    // Filter out the bad hashtags
    parsed.setHashtags(tweet.getHashtags().stream().filter(ht -> !BAD_HASHTAGS.contains(ht))
        .collect(Collectors.toSet()));

    // Parse the text to obtain tokens with POS and NER tags
    List<Token> tokens = new Parser(text).getTokens();
    parsed.setTokens(tokens);

    // Select key words
    Set<String> keywords = tokens.stream().filter(TweetParser::isKeyWord).map(Token::getText)
        .collect(Collectors.toSet());
    parsed.setKeywords(keywords);

    // Generate the key phrases
    // TODO there are better ways of doing this than getting the power set then filtering it
    Set<String> keyphrases =
        powerSet(keywords).stream().filter(set -> set.size() > 1 && set.size() < 5).map(Phrase::new)
            .map(Phrase::getText).collect(Collectors.toSet());
    parsed.setKeyphrases(keyphrases);

    // Save the parsed tweet
    ds.save(parsed);
  }

  private static boolean isKeyWord(Token t) {
    String lower = t.getText().toLowerCase();
    String pos = t.getPos();

    // Can be a noun or a verb
    if (!pos.startsWith("NN") && !pos.startsWith("VB"))
      return false;

    // URLs not allowed
    if (lower.startsWith("https://") || lower.startsWith("http://"))
      return false;

    // The word chatbot and variants are not allowed
    if (exactMatch(lower, CHATBOT_REGEX))
      return false;

    return true;
  }

  /**
   * @param keywords
   * @return the power set of {@code keywords}.
   */
  private static Set<Set<String>> powerSet(Set<String> keywords) {
    Set<Set<String>> sets = new HashSet<>();

    // Return a set containing a single empty set if originalSet is empty
    if (keywords.isEmpty()) {
      sets.add(new HashSet<>());
      return sets;
    }

    // Recursively construct the power set
    List<String> list = new ArrayList<>(keywords);
    String head = list.get(0);
    Set<String> rest = new HashSet<>(list.subList(1, list.size()));
    for (Set<String> set : powerSet(rest)) {
      Set<String> newSet = new HashSet<>();
      newSet.add(head);
      newSet.addAll(set);
      sets.add(newSet);
      sets.add(set);
    }

    return sets;
  }

  /**
   * @param s
   * @param regex
   * @return true if {s} exactly matches {@code regex}, false otherwise.
   */
  private static boolean exactMatch(String s, String regex) {
    return s.replaceFirst(regex, "").isEmpty();
  }

  public static void main(String[] args) {
    new TweetParser().run();
  }

}

