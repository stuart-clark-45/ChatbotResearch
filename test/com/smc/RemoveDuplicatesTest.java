package com.smc;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mongodb.morphia.Datastore;

import com.candmcomputing.util.MongoHelper;
import com.candmcomputing.util.Testing;
import com.smc.model.Tweet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author Stuart Clark
 */
@RunWith(Testing.class)
public class RemoveDuplicatesTest {

  private Datastore ds;

  @Before
  public void setUp() throws Exception {
    ds = MongoHelper.getDataStore();
    tweetWithId(1);
    tweetWithId(1);
    tweetWithId(1);
    tweetWithId(1);
    tweetWithId(1);
    tweetWithId(2);
  }

  private void tweetWithId(long id){
    Tweet tweet = new Tweet();
    tweet.setTweetId(id);
    ds.save(tweet);
  }

  @After
  public void tearDown() throws Exception {
    Testing.drop();
  }

  @Test
  public void test() throws Exception {
    new RemoveDuplicates().run();
    assertEquals(2, ds.createQuery(Tweet.class).count());
    HashSet<Long> ids = new HashSet<>();
    ds.createQuery(Tweet.class).forEach(t -> ids.add(t.getTweetId()));
    assertEquals(new HashSet<>(asList(1L, 2L)), ids);
  }

}
