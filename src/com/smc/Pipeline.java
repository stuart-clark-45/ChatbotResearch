package com.smc;

import java.io.IOException;

/**
 * Used to run the data pipeline.
 *
 * @author Stuart Clark
 */
public class Pipeline {

  private void run() throws IOException {
    new TweetParser().run();
    new Analysis().run();
  }

  public static void main(String[] args) throws IOException {
    new Pipeline().run();
  }

}
