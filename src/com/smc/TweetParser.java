package com.smc;

import com.candmcomputing.util.MongoHelper;
import com.smc.model.ParsedTweet;
import com.smc.model.StatusWrapper;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.mongodb.morphia.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.HashtagEntity;
import twitter4j.Status;

import java.util.List;
import java.util.Properties;

public class TweetParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(TweetParser.class);

  private final Datastore ds;
  private final StanfordCoreNLP pipeline;

  public TweetParser() {
    this.ds = MongoHelper.getDataStore();

    LOGGER.info("Initialising StanfordCoreNLP...");
    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
    this.pipeline = new StanfordCoreNLP(props);
  }

  private void run() {
    // Drop existing parsed tweets
    ds.getCollection(ParsedTweet.class).drop();

    // Iterate over all of the imported tweets
    LOGGER.info("Parsing " + ds.getCount(StatusWrapper.class) + " StatusWrappers...");
    for (StatusWrapper wrapper : ds.createQuery(StatusWrapper.class)) {

      // Create new parsed tweet
      ParsedTweet parsed = new ParsedTweet();

      for (HashtagEntity hashtagEntity : wrapper.getStatus().getHashtagEntities()) {
        LOGGER.info(hashtagEntity.getText());
      }


//      // Set unparsed
//      Status status = wrapper.getStatus();
//      String text = status.getText();
//      parsed.setUnparsed(text);
//
//      Annotation document = new Annotation(text);
//      pipeline.annotate(document);
//      document.get(TokensAnnotation.class);
//
//      List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
//      for (CoreMap sentence : sentences) {
//        SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation.class);
//        LOGGER.info("");
//      }

    }
  }

  public static void main(String[] args) {
    new TweetParser().run();
  }

}

