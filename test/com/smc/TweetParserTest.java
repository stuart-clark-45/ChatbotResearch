package com.smc;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mongodb.morphia.Datastore;

import com.candmcomputing.util.MongoHelper;
import com.candmcomputing.util.Testing;
import com.smc.model.ParsedTweet;
import com.smc.model.Tweet;

@RunWith(Testing.class)
public class TweetParserTest {

  private Tweet tweet;
  private Datastore ds;
  private String text;
  private Set<String> hashtags;

  @Before
  public void setUp() throws Exception {
    text = "I am #testing TweetParser, #123";
    hashtags = Stream.of("testing", "123").collect(Collectors.toSet());

    tweet = new Tweet();
    tweet.setText(text);
    tweet.setHashtags(hashtags);

    ds = MongoHelper.getDataStore();
    ds.save(tweet);
  }

  @After
  public void tearDown() throws Exception {
    Testing.drop();
  }

  @Test
  public void test() throws Exception {
    TweetParser.main(new String[0]);

    // Get all parsed tweets
    List<ParsedTweet> parsedTweets = ds.createQuery(ParsedTweet.class).asList();

    // Check only one
    assertEquals(1, parsedTweets.size());

    // Check it was correctly parsed
    ParsedTweet parsed = parsedTweets.get(0);
    Set<String> htUpper = hashtags.stream().map(String::toUpperCase).collect(Collectors.toSet());
    assertEquals(htUpper, parsed.getHashtags());
    assertEquals(tweet.getText(), parsed.getUnparsed());
    assertEquals(Stream.of("TESTING", "TWEETPARSER", "AM").collect(Collectors.toSet()),
        parsed.getKeywords());
    Set<String> expected = Stream
        .of("AM TESTING TWEETPARSER", "TESTING TWEETPARSER", "AM TESTING", "AM TWEETPARSER",
            "123 AM TESTING TWEETPARSER", "123 TESTING TWEETPARSER", "123 AM TESTING",
            "123 AM TWEETPARSER", "123 TWEETPARSER", "123 TESTING", "123 AM")
        .collect(Collectors.toSet());
    assertEquals(expected, parsed.getKeyphrases());
  }

}
