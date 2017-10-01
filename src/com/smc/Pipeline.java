package com.smc;

import java.io.IOException;

/**
 * Used to run the data pipeline.
 *
 * @author Stuart Clark
 */
public class Pipeline {

  public static void main(String[] args) throws IOException {
    new TweetParser().run();
    new Analysis().run();
  }

}
