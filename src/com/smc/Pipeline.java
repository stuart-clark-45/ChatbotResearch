package com.smc;

/**
 * Used to run the data pipeline.
 *
 * @author Stuart Clark
 */
public class Pipeline {

  public static void main(String[] args) {
    new TweetParser().run();
    new Analysis().run();
  }

}
