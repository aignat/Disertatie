
package utils;

import java.io.File;

/**
 *
 * @author aignat
 */
public class Constants {

    public static final int SMOOTHING = 2;
    public static final int STARTING_YEAR = 1800;
    public static final int END_YEAR = 2008;

    public static final String WORDNET_FILE_PROPERTIES = "resources" + File.separator + "jwnl14-rc2" + File.separator + "config" + File.separator + "file_properties.xml";
    public static final String WORDNET_WORDS_FILE = "all_WordNet_words.txt";
    public static final String WORDNET_WORDS_SYNONYMS_FILE = "all_WordNet_words_and_synonyms.txt";
    public static final String WORDNET_WORDS_PEAKYEARS_FILE = "all_WordNet_peaks.txt";

    public static final String NGRAM_DATA_FOR_ALL_WORDS = "resources" + File.separator + "englishNgramData.txt";
    public static final String ENGLISH_1GRAM_CSV_PREFIX = "googlebooks-eng-all-1gram-20120701-";
    public static final String TOTAL_COUNTS_FILENAME = "resources" + File.separator + "GoogleNgrams" + File.separator + "googlebooks-eng-all-totalcounts-20120701.txt";
    public static final String CORPUS_PATH = "resources" + File.separator + "GoogleNgrams";

    public static final String ENGLISH_CORPUS_NAME = "English";

}
