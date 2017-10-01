package com.smc.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Stuart Clark
 */
public class BandWordsTest {

  @Test
  public void test() throws Exception {
    BandWords bandWords = new BandWords("testres/band-words.txt");
    assertTrue(bandWords.isBanned("hello "));
    assertTrue(bandWords.isBanned(" WoRLD"));
    assertFalse(bandWords.isBanned("testing"));
  }

}
