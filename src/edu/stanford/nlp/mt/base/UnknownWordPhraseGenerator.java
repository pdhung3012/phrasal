package edu.stanford.nlp.mt.base;

import java.util.*;

import edu.stanford.nlp.mt.decoder.feat.RuleFeaturizer;

/**
 *
 * @author danielcer
 *
 * @param <TK>
 */
public class UnknownWordPhraseGenerator<TK, FV> extends
    AbstractPhraseGenerator<TK, FV> implements DynamicPhraseGenerator<TK,FV> {

  public static final String PHRASE_TABLE_NAMES = "IdentityPhraseGenerator(Dyn)";
  public static final String[] DEFAULT_SCORE_NAMES = { "p_i(t|f)" };
  public static final float[] SCORE_VALUES = { (float) 1.0 };
  public static final String DEBUG_PROPERTY = "UnknownWordPhraseGeneratorDebug";
  public static final boolean DEBUG = Boolean.parseBoolean(System.getProperty(
      DEBUG_PROPERTY, "false"));

  // do we need to account for "(0) (1)", etc?
  public static final PhraseAlignment DEFAULT_ALIGNMENT = PhraseAlignment
      .getPhraseAlignment("I-I");

  private final String[] scoreNames;
  private final SequenceFilter<TK> filter;
  final boolean dropUnknownWords;
  private RawSequence<TK> empty = new RawSequence<TK>();

  /**
   *
   */
  public UnknownWordPhraseGenerator(
      RuleFeaturizer<TK, FV> phraseFeaturizer, boolean dropUnknownWords,
      SequenceFilter<TK> filter) {
    super(phraseFeaturizer);
    this.filter = filter;
    scoreNames = DEFAULT_SCORE_NAMES;
    this.dropUnknownWords = dropUnknownWords;
  }

  /**
   *
   */
  public UnknownWordPhraseGenerator(
      RuleFeaturizer<TK, FV> phraseFeaturizer, boolean dropUnknownWords,
      SequenceFilter<TK> filter, String scoreName) {
    super(phraseFeaturizer);
    this.filter = filter;
    scoreNames = new String[] { scoreName };
    this.dropUnknownWords = dropUnknownWords;
  }

  /**
   *
   */
  public UnknownWordPhraseGenerator(
      RuleFeaturizer<TK, FV> phraseFeaturizer, boolean dropUnknownWords) {
    super(phraseFeaturizer);
    this.filter = null;
    scoreNames = DEFAULT_SCORE_NAMES;
    this.dropUnknownWords = dropUnknownWords;
  }

  public UnknownWordPhraseGenerator(
      RuleFeaturizer<TK, FV> phraseFeaturizer, boolean dropUnknownWords,
      String scoreName) {
    super(phraseFeaturizer);
    this.filter = null;
    scoreNames = new String[] { scoreName };
    this.dropUnknownWords = dropUnknownWords;
  }

  @Override
  public String getName() {
    return PHRASE_TABLE_NAMES;
  }

  @Override
  public List<Rule<TK>> getTranslationOptions(Sequence<TK> sequence) {
    List<Rule<TK>> list = new LinkedList<Rule<TK>>();
    RawSequence<TK> raw = new RawSequence<TK>(sequence);
    if (filter == null || filter.accepts(raw)) {
      String word = raw.toString();

      if (dropUnknownWords && !isNumeric(word) && !isASCII(word)) {
          list.add(new Rule<TK>(SCORE_VALUES, scoreNames, empty, raw,
          DEFAULT_ALIGNMENT));
      } else {
    	  list.add(new Rule<TK>(SCORE_VALUES, scoreNames, raw, raw,
              DEFAULT_ALIGNMENT));
      }
    }
    return list;
  }

  // TODO make this more general in the future by matching to unicode pages of the target language
  private static boolean isASCII(String word) {
	return word.matches("\\p{ASCII}*");
  }

  private static boolean isNumeric(String word) {
	return word.matches(".*[0-9\\.\\\\/,:-]+[%A-Za-z]*");
  }

  @Override
  public int longestSourcePhrase() {
    return -Integer.MAX_VALUE;
  }

  @Override
  public void setCurrentSequence(Sequence<TK> foreign,
      List<Sequence<TK>> tranList) {
    // no op
  }

}
