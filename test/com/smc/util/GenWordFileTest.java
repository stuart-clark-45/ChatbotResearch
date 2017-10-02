package com.smc.util;

import static java.nio.file.Files.readAllLines;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mongodb.morphia.Datastore;

import com.candmcomputing.util.MongoHelper;
import com.candmcomputing.util.Testing;
import com.smc.model.HashTag;
import com.smc.model.KeyWord;

/**
 * @author Stuart Clark
 */
@RunWith(Testing.class)
public class GenWordFileTest {

  private static final String FILE_NAME = "test.txt";

  private Datastore ds;

  @Before
  public void setUp() throws Exception {
    ds = MongoHelper.getDataStore();

    HashTag ht = new HashTag();
    ht.setString("hello");
    ht.setCount(2);
    ds.save(ht);

    ht = new HashTag();
    ht.setString("world");
    ht.setCount(4);
    ds.save(ht);

    KeyWord kw = new KeyWord();
    kw.setString("hello");
    kw.setCount(3);
    ds.save(kw);
  }

  @After
  public void tearDown() throws Exception {
    Testing.drop();
    new File(FILE_NAME).delete();
  }

  @Test
  public void test() throws Exception {
    new GenWordFile(FILE_NAME).run();
    String content =
        String.join("\n", readAllLines(Paths.get(FILE_NAME), Charset.defaultCharset()));
    String expected = "hello\nworld";
    assertEquals(expected, content);
  }

}
