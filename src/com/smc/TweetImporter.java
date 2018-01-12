package com.smc;

import static com.candmcomputing.util.ConfigHelper.getString;
import static org.apache.commons.lang3.ArrayUtils.toArray;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.candmcomputing.util.ConfigHelper;
import com.candmcomputing.util.MongoHelper;
import com.smc.model.StatusWrapper;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Used to import tweets relating to chatbots into the database using the twitter stream api.
 *
 * @author Stuart Clark
 */
public class TweetImporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(TweetImporter.class);
  private static final String[] SCREEN_NAMES;
  private static final String[] NO_AT_SCREEN_NAMES;
  private static final String ENGLISH = "en";

  static {
    // Create array of screen names
    List<String> list = ConfigHelper.getList(String.class, "screenNames");
    int numNames = list.size();
    SCREEN_NAMES = list.toArray(new String[numNames]);

    // Create array of screen names without an @ at the start
    List<String> noAt = list.stream().map(s -> s.substring(1)).collect(Collectors.toList());
    NO_AT_SCREEN_NAMES = noAt.toArray(new String[numNames]);
  }

  private final Configuration config;

  public TweetImporter() {
    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setOAuthConsumerKey(getString("twitter.consumerKey"));
    cb.setOAuthConsumerSecret(getString("twitter.consumerSecret"));
    cb.setOAuthAccessToken(getString("twitter.accessToken"));
    cb.setOAuthAccessTokenSecret(getString("twitter.accessTokenSecret"));
    config = cb.build();
  }

  public void run() throws TwitterException, InterruptedException {
    // Run the tweet streams in parallel
    List<Runnable> tweetStreams = Arrays.asList(this::keyWords, this::feeds);
    tweetStreams.parallelStream().forEach(Runnable::run);
  }

  private void keyWords() {
    TwitterStream twitterStream = new TwitterStreamFactory(config).getInstance();
    twitterStream.addListener(new Listener("tweets found using key words", false));
    FilterQuery query = new FilterQuery().language(ENGLISH).track(SCREEN_NAMES);
    twitterStream.filter(query);
  }

  private void feeds() {
    try {
      // Get array of user ids
      TwitterFactory tf = new TwitterFactory(config);
      Twitter twitter = tf.getInstance();
      List<Long> idList = twitter.lookupUsers(NO_AT_SCREEN_NAMES).stream().map(User::getId).collect(Collectors.toList());
      long[] idArray = ArrayUtils.toPrimitive(idList.toArray(new Long[idList.size()]));

      // Steam tweets from user feeds
      TwitterStream twitterStream = new TwitterStreamFactory(config).getInstance();
      twitterStream.addListener(new Listener("tweets found by tracking feeds", true));
      FilterQuery query = new FilterQuery().language(ENGLISH).follow(idArray);
      twitterStream.filter(query);
    } catch (TwitterException e) {
      throw new IllegalStateException("Failed connect to user feeds", e);
    }
  }

  /**
   * Used to handle events related to the the twitter stream api.
   */
  private static class Listener extends StatusAdapter {

    private AtomicInteger counter;
    private String loggingMsg;
    private boolean fromFeed;

    public Listener(String loggingMsg, boolean fromFeed) {
      this.fromFeed = fromFeed;
      this.counter = new AtomicInteger();
      this.loggingMsg = loggingMsg;
    }

    @Override
    public void onStatus(Status status) {
      // Save the tweet
      StatusWrapper wrapper = new StatusWrapper(status);
      if (fromFeed) {
        wrapper.setFromFeed(false);
      }
      MongoHelper.getDataStore().save(wrapper);

      // Logging
      int count = counter.incrementAndGet();
      if (count % 10 == 0) {
        LOGGER.info(count + " " + loggingMsg);
      }
    }

    @Override
    public void onStallWarning(StallWarning warning) {
      LOGGER.warn(warning.toString());
    }

    @Override
    public void onException(Exception e) {
      LOGGER.error("Error whilst streaming tweets", e);
    }

  }

  public static void main(String[] args) throws TwitterException, InterruptedException {
    new TweetImporter().run();
  }

}
