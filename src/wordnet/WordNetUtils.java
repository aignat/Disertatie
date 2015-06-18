package wordnet;

import exception.CustomException;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.*;
import utils.Constants;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;

/**
 * @author aignat
 */
public class WordNetUtils {

    /**
     * initialize WordNetUtils
     *
     * @throws JWNLException
     * @throws FileNotFoundException
     */
    public static void initializeWordNet() throws CustomException {
        try {
            JWNL.initialize(new FileInputStream(Constants.WORDNET_FILE_PROPERTIES));
        } catch (JWNLException e) {
            throw new CustomException("Can't initialize WordNetUtils:", Thread.currentThread().getStackTrace()[1].getMethodName());
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

    public static HashSet<String> getHypernymsAndHyponyms(IndexWord indexWord) throws CustomException {

        HashSet<String> hSet = new HashSet<String>();

        try {
            for (Synset s : indexWord.getSenses()) {
                for (Pointer x : s.getPointers(PointerType.HYPONYM)) {
                    for (Word w : x.getTargetSynset().getWords()) {
                            hSet.add(w.getLemma());
                    }
                }
                for (Pointer x : s.getPointers(PointerType.HYPERNYM)) {
                    for (Word w : x.getTargetSynset().getWords()) {
                            hSet.add(w.getLemma());
                    }
                }
            }
        } catch (JWNLException e) {
            throw new CustomException("Can't get hypernym or hyponym for " + indexWord.getLemma(), Thread.currentThread().getStackTrace()[1].getMethodName());
        }

        return hSet;
    }

}
