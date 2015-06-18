
package utils;

import java.io.File;

/**
 *
 * @author aignat
 */
public class Constants {

    public static final String RESULTS = "results.txt";

    public static final String WORDNET_FILE_PROPERTIES = "resources" + File.separator + "jwnl14-rc2" + File.separator + "config" + File.separator + "file_properties.xml";

    public static final int NGRAM_SMOOTHING = 2;
    public static final int NGRAM_START_YEAR = 1800;
    public static final int NGRAM_END_YEAR = 2008;
    public static final String NGRAM_ENGLISH_TOTALCOUNTS_FILE = "resources" + File.separator + "GoogleNgrams" + File.separator + "googlebooks-eng-all-totalcounts-20120701.txt";
}
