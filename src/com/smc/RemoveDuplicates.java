package com.smc;

import static org.mongodb.morphia.aggregation.Group.grouping;

import java.util.Iterator;
import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.aggregation.Accumulator;
import org.mongodb.morphia.query.Query;

import com.candmcomputing.util.MongoHelper;
import com.smc.model.LongCountResult;
import com.smc.model.Tweet;

/**
 * Used to remove duplicate tweets should they appear in the database.
 *
 * @author Stuart Clark
 */
public class RemoveDuplicates {

  private final Datastore ds;
  private final Accumulator counter;

  public RemoveDuplicates() {
    this.ds = MongoHelper.getDataStore();
    this.counter = new Accumulator("$sum", 1);
  }

  public void run() {
    // Query that matches documents with a count of greater than 1 (duplicates)
    Query<LongCountResult> match =
        ds.createQuery(LongCountResult.class).field("count").greaterThan(1);

    // Aggregate the Tweets to find the tweet id's of the duplicates
    Iterator<LongCountResult> aggregation = ds.createAggregation(Tweet.class)
        .group("tweetId", grouping("count", counter)).match(match).aggregate(LongCountResult.class);

    // Remove the duplicates
    aggregation.forEachRemaining((result) -> {
      List<Tweet> dupes =
          ds.createQuery(Tweet.class).field("tweetId").equal(result.getLlong()).asList();
      for (int i = 0; i < result.getCount() - 1; i++) {
        ds.delete(dupes.get(i));
      }
    });
  }

  public static void main(String[] args) {
    new RemoveDuplicates().run();
  }

}
