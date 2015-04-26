package wordnet;

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
    public static void initializeWordNet() throws JWNLException, FileNotFoundException {
        JWNL.initialize(new FileInputStream(Constants.WORDNET_FILE_PROPERTIES));
    }

    
    /**
     * @param inputIndexWord
     * @return all synonyms of inputWord
     */
    public static HashSet<String> getSynonyms(IndexWord inputIndexWord) {

        HashSet<String> synonimsSet = new HashSet<String>();

        Synset[] synSets = null;
        try {
            synSets = inputIndexWord.getSenses();
        } catch (JWNLException e) {
            e.printStackTrace();
            return synonimsSet;
        }

        for (Synset synset : synSets) {
            Word[] words = synset.getWords();
            for (Word word : words) {
                synonimsSet.add(word.getLemma());
            }
        }

        return synonimsSet;
    }

    
    public static void writeAllWordsWithSynonymsToFile(String fileName) {

        File file = new File(fileName);
        BufferedWriter output = null;

        try {
            output = new BufferedWriter(new FileWriter(file));

            for (Object pos : POS.getAllPOS()) {
                try {
                    Iterator iterator = Dictionary.getInstance().getIndexWordIterator((POS) pos);

                    while (iterator.hasNext()) {
                        IndexWord indexWord = (IndexWord) iterator.next();

                        output.write(indexWord.getLemma() + ":");

                        for (String synonym : getSynonyms(indexWord)) {
                            synonym = synonym.replace('_', ' ');
                            output.write(synonym);
                            output.write(",");
                        }
                        output.newLine();
                    }
                } catch (JWNLException ex) {
                    System.out.println("Error getting words list from WordNet");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
