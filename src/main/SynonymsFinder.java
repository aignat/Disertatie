package main;

import exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wordnet.WordNet;

/**
 * @author aignat
 */
public class SynonymsFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(SynonymsFinder.class);

    public static void main(String[] args) {

        //initialize WordNet
        try {
            WordNet.initializeWordNet();
        } catch (CustomException e) {
            LOGGER.error(e.getMessage() + ":" + e.getOriginatingMethodName());
            return;
        }

        //write all the WordNet words and their synonyms to Constants.WORDNET_WORDS_SYNONYMS_FILE
        try {
            WordNet.writeAllWordsWithSynonymsToFile();
        } catch (CustomException e) {
            LOGGER.error(e.getMessage() + ":" + e.getOriginatingMethodName());
        }
    }
}

