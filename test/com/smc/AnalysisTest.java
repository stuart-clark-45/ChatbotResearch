package com.smc;

import static com.candmcomputing.util.SetUtils.asSet;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mongodb.morphia.Datastore;

import com.candmcomputing.util.MongoHelper;
import com.candmcomputing.util.Testing;
import com.smc.model.HashTag;
import com.smc.model.KeyPhrase;
import com.smc.model.KeyWord;
import com.smc.model.ParsedTweet;
import com.smc.model.StringCountResult;

/**
 * @author Stuart Clark
 */
@RunWith(Testing.class)
public class AnalysisTest {

  private static final String ONE = "one";
  private static final String TWO = "two";
  private static final String THREE = "three";
  private static final String HT = "ht";
  private static final String KW = "kw";
  private static final String KP = "kp";

  private Datastore ds;

  @Before
  public void setUp() throws Exception {
    ds = MongoHelper.getDataStore();
    ParsedTweet parsed = new ParsedTweet();
    parsed.setHashtags(asSet(HT + ONE, HT + TWO, HT + THREE));
    parsed.setKeywords(asSet(KW + ONE, KW + TWO, KW + THREE));
    parsed.setKeyphrases(asSet(KP + ONE, KP + TWO, KP + THREE));
    ds.save(parsed);

    parsed = new ParsedTweet();
    parsed.setHashtags(asSet(HT + TWO, HT + THREE));
    parsed.setKeywords(asSet(KW + TWO, KW + THREE));
    parsed.setKeyphrases(asSet(KP + TWO, KP + THREE));
    ds.save(parsed);

    parsed = new ParsedTweet();
    parsed.setHashtags(asSet(HT + THREE));
    parsed.setKeywords(asSet(KW + THREE));
    parsed.setKeyphrases(asSet(KP + THREE));
    ds.save(parsed);
  }

  @After
  public void tearDown() throws Exception {
    Testing.drop();
  }

  @Test
  public void test() throws Exception {
    new Analysis().run();
    checkAggregation(HashTag.class, HT);
    checkAggregation(KeyWord.class, KW);
    checkAggregation(KeyPhrase.class, KP);
  }

  private <T extends StringCountResult> void checkAggregation(Class<T> clazz, String prefix)
      throws Exception {
    List<T> keyWords = ds.createQuery(clazz).asList();
    assertEquals(3, keyWords.size());
    Set<T> actual = new HashSet<>(keyWords);

    T one = clazz.newInstance();
    one.setString(prefix + ONE);
    one.setCount(1);
    one.setRank(3);

    T two = clazz.newInstance();
    two.setString(prefix + TWO);
    two.setCount(2);
    two.setRank(2);

    T three = clazz.newInstance();
    three.setString(prefix + THREE);
    three.setCount(3);
    three.setRank(1);

    Set<T> expected = asSet(one, two, three);

    assertEquals(expected, actual);
  }

}
