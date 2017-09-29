package com.smc;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.candmcomputing.util.MongoHelper;
import com.smc.model.Tweet;

import twitter4j.FilterQuery;
import twitter4j.HashtagEntity;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class TweetImporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(TweetImporter.class);

  private AtomicInteger counter;

  public TweetImporter() {
    counter = new AtomicInteger();
  }

  public void run() throws TwitterException, InterruptedException {
    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setOAuthConsumerKey("***REMOVED***");
    cb.setOAuthConsumerSecret("***REMOVED***");
    cb.setOAuthAccessToken("***REMOVED***");
    cb.setOAuthAccessTokenSecret("***REMOVED***");

    StatusListener listener = new StatusListener() {

      @Override
      public void onStatus(Status status) {
        // Get the user
        User user = status.getUser();

        // Get the hashtags
        Set<String> hashtags = new HashSet<>();
        for (HashtagEntity hashtag : status.getHashtagEntities()) {
          hashtags.add(hashtag.toString());
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
      public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}

      @Override
      public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

      @Override
      public void onScrubGeo(long userId, long upToStatusId) {}

      @Override
      public void onStallWarning(StallWarning warning) {
        LOGGER.warn(warning.toString());
        System.exit(0);
      }

      @Override
      public void onException(Exception e) {
        LOGGER.error("Error whilst streaming tweets", e);
      }
    };


    TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
    twitterStream.addListener(listener);

    FilterQuery query = new FilterQuery().language("en").track("chatbot", "chatbots");
    twitterStream.filter(query);


    Thread.sleep(400000);

  }

  public static void main(String[] args) throws TwitterException, InterruptedException {
    new TweetImporter().run();
  }

}
