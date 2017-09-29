package com.smc;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.mongodb.morphia.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.candmcomputing.util.ConfigHelper;
import com.candmcomputing.util.MongoHelper;
import com.smc.model.ParsedTweet;
import com.smc.model.Token;
import com.smc.model.Tweet;
import com.smc.util.Parser;

public class TweetParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(TweetParser.class);
  private static final Set<String> BAD_HASHTAGS =
      new HashSet<>(ConfigHelper.getList(String.class, "hashtags.exclude"));

  private final Datastore ds;

  public TweetParser() {
    this.ds = MongoHelper.getDataStore();

    LOGGER.info("Initialising StanfordCoreNLP...");
    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
  }

  private void run() {
    // Drop existing parsed tweets
    ds.getCollection(ParsedTweet.class).drop();

    // Iterate over all of the imported tweets
    LOGGER.info("Parsing " + ds.getCount(Tweet.class) + " tweets...");

    // Parse the tweets in parallel
    StreamSupport.stream(ds.createQuery(Tweet.class).spliterator(), true).forEach(this::parseTweet);
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

    // Filter out the bad hashtags
    parsed.setHashtags(tweet.getHashtags().stream().filter(ht -> !BAD_HASHTAGS.contains(ht))
        .collect(Collectors.toSet()));

    // Parse the text to obtain tokens with POS and NER tags
    List<Token> tokens = new Parser(text).getTokens();
    parsed.setTokens(tokens);

    // Select key words
    parsed.setKeywords(tokens.stream().filter(TweetParser::isKeyWord).map(Token::getText)
        .collect(Collectors.toSet()));

    // Save the parsed tweet
    ds.save(parsed);
  }

      // Filter out the bad hashtags
      parsed.setHashtags(tweet.getHashtags().stream().filter(ht -> !BAD_HASHTAGS.contains(ht))
          .collect(Collectors.toSet()));

      // Parse the text to obtain tokens with POS and NER tags
      List<Token> tokens = new Parser(text).getTokens();
      parsed.setTokens(tokens);

      // Select key words
      parsed.setKeywords(tokens.stream().filter(TweetParser::isKeyWord).map(Token::getText)
          .collect(Collectors.toSet()));

      // Save the parsed tweet
      ds.save(parsed);
    }
  }

  private static boolean isKeyWord(Token t) {
    return t.getPos().startsWith("NN") || t.getPos().startsWith("VB");
  }

  public static void main(String[] args) {
    new TweetParser().run();
  }

}

