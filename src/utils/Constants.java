
package utils;

import java.io.File;

/**
 *
 * @author aignat
 */
public class Constants {

    public static final String WORDNET_FILE_PROPERTIES = "resources" + File.separator + "jwnl14-rc2" + File.separator + "config" + File.separator + "file_properties.xml";
    public static final String WORDNET_WORDS_SYNONYMS_FILE = "all_WordNet_words_and_synonyms.txt";
    public static final String WORDNET_WORDS_PEAKYEARS_FILE = "all_WordNet_peaks.txt";
    public static final String WORDNET_WORDS_INTERSECTIONS = "all_WordNet_intersections.txt";

    public static final int NGRAM_SMOOTHING = 2;
    public static final int NGRAM_START_YEAR = 1800;
    public static final int NGRAM_END_YEAR = 2008;
    public static final String NGRAM_ALL_WORDS = "distinctEnglish1Grams.txt";
    public static final String NGRAM_ENGLISH_TOTALCOUNTS_FILE = "resources" + File.separator + "GoogleNgrams" + File.separator + "googlebooks-eng-all-totalcounts-20120701.txt";
}
