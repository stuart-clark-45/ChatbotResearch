package com.smc;

import com.smc.util.CbrException;

/**
 * Used to run the data pipeline.
 *
 * @author Stuart Clark
 */
public class Pipeline {

  private void run() throws CbrException {
    new TweetParser().run();
    new Analysis().run();
  }

  public static void main(String[] args) throws CbrException {
    new Pipeline().run();
  }

}
