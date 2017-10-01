package com.smc;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.candmcomputing.util.ConfigHelper;
import com.candmcomputing.util.MongoHelper;
import com.smc.model.ParsedTweet;

/**
 * Used to search for tweets via a command line interface.
 *
 * @author Stuart Clark
 */
public class TweetSearch {

  private static final boolean IGNORE_RT = ConfigHelper.getBoolean("ignoreRetweets");

  private final Datastore ds;
  private final Scanner sc;
  private final List<String> options;
  private String field;
  private String fieldSingular;

  private TweetSearch() {
    this.ds = MongoHelper.getDataStore();
    this.sc = new Scanner(System.in);
    this.options = Arrays.asList("keywords", "keyphrases", "hashtags");
  }

  public void run() {

    // Get the field the user wants to search for
    getField();

    // Allow the user to search for tweets
    while (true) {

      // Get the value the user wants to search for
      System.out.println("\nEnter the " + fieldSingular
          + " that you would like to search for or use ctlr+c to quit:");
      String value = sc.nextLine().toUpperCase();

      // Search for tweets and print them out
      System.out.println("\nTweets: ");
      Query<ParsedTweet> query = ds.createQuery(ParsedTweet.class).field(field).equal(value);
      if (IGNORE_RT) {
        query.field("retweet").equal(false);
      }
      int counter = 0;
      for (ParsedTweet tweet : query) {
        System.out.println(++counter + ") " + tweet.getUnparsed());
      }

    }
  }

  /**
   * Get the field the user wants to search for using the terminal.
   */
  private void getField() {
    while (field == null) {
      System.out.println(
          "\nSelect how you would like to search for tweets:\n\n1) keywords \n2) keyphrases \n3) hashtags\n\n"
              + "Please enter either 1, 2 or 3...");

      String choice = sc.next();
      if (choice.equals("1") || choice.equals("2") || choice.equals("3")) {
        field = options.get(Integer.parseInt(choice) - 1);
      }
    }
    fieldSingular = field.substring(0, field.length() - 1);
  }

  public static void main(String[] args) {
    new TweetSearch().run();
  }

}
