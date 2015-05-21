package main;

import exception.CustomException;
import ngrams.NGramUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by aignat on 5/14/2015.
 */
public class PeaksFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeaksFinder.class);

    public static void main(String[] args) {

        //write peak years for all WordNet words to Constants.WORDNET_WORDS_PEAKYEARS_FILE
        try {
            NGramUtils.writePeaksForAllWordNetWordsToFile();
        } catch (CustomException e) {
            LOGGER.error(e.getMessage() + ":" + e.getOriginatingMethodName());
        }

        System.out.println("Done finding peaks");
    }
}
