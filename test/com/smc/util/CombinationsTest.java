package com.smc.util;

import static com.candmcomputing.util.SetUtils.asSet;
import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Test;

public class CombinationsTest {

  @Test
  public void test() throws Exception {
    Set<Integer> vals = asSet(1, 2, 3);

    Set<Set<Integer>> actual;
    Set<Set<Integer>> expected;

    actual = new Combinations<>(vals, 2, 2).find();
    expected = asSet(asSet(1, 2), asSet(1, 3), asSet(2, 3));
    assertEquals(expected, actual);

    actual = new Combinations<>(vals, 0, 0).find();
    expected = asSet();
    assertEquals(expected, actual);

    actual = new Combinations<>(vals, 1, 3).find();
    expected =
        asSet(asSet(1), asSet(2), asSet(3), asSet(1, 2), asSet(1, 3), asSet(2, 3), asSet(1, 2, 3));
    assertEquals(expected, actual);

    actual = new Combinations<>(vals, 2, 3).find();
    expected = asSet(asSet(1, 2), asSet(1, 3), asSet(2, 3), asSet(1, 2, 3));
    assertEquals(expected, actual);
  }

}
