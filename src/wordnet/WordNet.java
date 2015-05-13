package wordnet;

import exception.CustomException;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;
import utils.Constants;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author aignat
 */
public class WordNet {

    /**
     * initialize WordNet
     *
     * @throws JWNLException
     * @throws FileNotFoundException
     */
    public static void initializeWordNet() throws CustomException {
        try {
            JWNL.initialize(new FileInputStream(Constants.WORDNET_FILE_PROPERTIES));
        } catch (JWNLException e) {
            throw new CustomException("Can't initialize WordNet:", Thread.currentThread().getStackTrace()[1].getMethodName());
        } catch (FileNotFoundException e) {
            throw new CustomException("Constants.WORDNET_FILE_PROPERTIES not found", Thread.currentThread().getStackTrace()[1].getMethodName());
        }
    }

    
    /**
     * @param inputIndexWord
     * @return all synonyms of inputWord
     */
    public static HashSet<String> getSynonyms(IndexWord inputIndexWord) throws CustomException {

        HashSet<String> synonymsSet = new HashSet<String>();

        try {
            for (Synset synset : inputIndexWord.getSenses()) {
                for (Word word : synset.getWords()) {
                    synonymsSet.add(word.getLemma());
                }
            }
        } catch (Exception e) {
            throw new CustomException("Can't get senses for " + inputIndexWord.getLemma(), Thread.currentThread().getStackTrace()[1].getMethodName());
        }

        return synonymsSet;
    }

    
    public static void writeAllWordsWithSynonymsToFile() throws CustomException {

        BufferedWriter output = null;

        try {
            output = new BufferedWriter(new FileWriter(Constants.WORDNET_WORDS_SYNONYMS_FILE));

            for (Object pos : POS.getAllPOS()) {
                try {
                    Iterator iterator = Dictionary.getInstance().getIndexWordIterator((POS) pos);

                    while (iterator.hasNext()) {
                        IndexWord indexWord = (IndexWord) iterator.next();

                        output.write(indexWord.getLemma() + ":");

                        try {
                            for (String synonym : getSynonyms(indexWord)) {
                                synonym = synonym.replace('_', ' ');
                                output.write(synonym + ",");
                            }
                        } catch (CustomException e) {
                            //do nothing, if Exception then no synonyms for current word, just continue to next word
                        }

                        output.newLine();
                    }
                } catch (JWNLException ex) {
                    //do nothing, if Exception then no synonyms for current word, just continue
                }
            }
        } catch (IOException e) {
            throw new CustomException("Can't write to Constants.WORDNET_WORDS_SYNONYMS_FILE", Thread.currentThread().getStackTrace()[1].getMethodName());
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    throw new CustomException("Error closing Constants.WORDNET_WORDS_SYNONYMS_FILE", Thread.currentThread().getStackTrace()[1].getMethodName());
                }
            }
        }

    }

}
