package com.smc;

import static com.candmcomputing.util.ConfigHelper.getString;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.candmcomputing.util.ConfigHelper;
import com.candmcomputing.util.MongoHelper;
import com.smc.model.Tweet;

import twitter4j.FilterQuery;
import twitter4j.HashtagEntity;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Used to import tweets relating to chatbots into the database using the twitter stream api.
 *
 * @author Stuart Clark
 */
public class TweetImporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(TweetImporter.class);
  private static final List<String> KEY_WORDS = ConfigHelper.getList(String.class, "keyWords");

  private AtomicInteger counter;

  public TweetImporter() {
    counter = new AtomicInteger();
  }

  public void run() throws TwitterException, InterruptedException {
    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setOAuthConsumerKey(getString("twitter.consumerKey"));
    cb.setOAuthConsumerSecret(getString("twitter.consumerSecret"));
    cb.setOAuthAccessToken(getString("twitter.accessToken"));
    cb.setOAuthAccessTokenSecret(getString("twitter.accessTokenSecret"));

    TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
    twitterStream.addListener(new Listener());

    FilterQuery query =
        new FilterQuery().language("en").track(KEY_WORDS.toArray(new String[KEY_WORDS.size()]));
    twitterStream.filter(query);
  }

  /**
   * Used to handle events related to the the twitter stream api.
   */
  private class Listener extends StatusAdapter {

    @Override
    public void onStatus(Status status) {
      // Get the user
      User user = status.getUser();

      // Get the hashtags
      Set<String> hashtags = new HashSet<>();
      for (HashtagEntity hashtag : status.getHashtagEntities()) {
        hashtags.add(hashtag.getText());
      }

      // Build the tweet
      Tweet tweet = new Tweet();
      tweet.setText(status.getText());
      tweet.setCreated(status.getCreatedAt());
      tweet.setTweetId(status.getId());
      tweet.setUserId(user.getId());
      tweet.setUsername(user.getName());
      tweet.setHashtags(hashtags);

      // Save the tweet
      MongoHelper.getDataStore().save(tweet);

      // Logging
      int count = counter.incrementAndGet();
      if (count % 10 == 0) {
        LOGGER.info(count + " tweets imported");
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
