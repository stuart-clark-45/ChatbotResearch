package com.smc.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smc.model.Token;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * Used to parse text and annotate it using SNLP.
 *
 * @auther Stuart Clark
 */
public class Parser {

  private static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);
  private static final StanfordCoreNLP PIPELINE;

  static {
    LOGGER.info("Initialising StanfordCoreNLP...");
    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos");
    PIPELINE = new StanfordCoreNLP(props);
  }

  private String s;
  private Annotation document;

  public Parser(String s) {
    this.s = s;
  }

  /**
   * @return lazily instantiated {@code document}.
   */
  private Annotation getDocument() {
    if (document == null) {
      document = new Annotation(s);
      PIPELINE.annotate(document);
    }
    return document;
  }

  /**
   * Parses {@code document} into a list of {@link Token}s. The list is in the same order in which
   * the tokens apear in {@code s}.
   *
   * @return the resultant list of {@link Token}s.
   */
  public List<Token> getTokens() {
    List<CoreLabel> labels = getDocument().get(CoreAnnotations.TokensAnnotation.class);
    List<Token> parsed = new ArrayList<>(labels.size());
    for (CoreLabel cl : labels) {
      Token token = new Token(cl.get(CoreAnnotations.TextAnnotation.class));
      token.setPos(cl.get(CoreAnnotations.PartOfSpeechAnnotation.class));
      parsed.add(token);
    }
    return parsed;
  }

}
