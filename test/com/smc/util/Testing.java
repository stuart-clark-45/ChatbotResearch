package com.smc.util;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.candmcomputing.model.Mode;
import com.candmcomputing.util.ConfigHelper;
import com.candmcomputing.util.MongoHelper;

/**
 * Runs code to be executed before all tests. And provides generic testing utilities.
 *
 * @author Stuart Clark.
 */
public class Testing extends BlockJUnit4ClassRunner {

  public static final String DB_NAME = "junitdb";
  private static final Logger LOGGER = LoggerFactory.getLogger(Testing.class);

  /**
   * Creates a {@link BlockJUnit4ClassRunner} to run {@code clazz}.
   *
   * @param clazz
   * @throws InitializationError if the test class is malformed.
   */
  public Testing(Class<?> clazz) throws InitializationError {
    super(clazz);

    // Set up database
    LOGGER.info("Using " + DB_NAME + "as database");
    ConfigHelper.setProperty("db", DB_NAME);
    drop();

    // Set application mode
    ConfigHelper.setProperty(Mode.KEY, Mode.Value.TEST.name());
    LOGGER.info("Config set up");
  }

  /**
   * Drop the database.
   */
  public static void drop() {
    MongoHelper.getDataStore().getDB().dropDatabase();
  }

}
