
package ngrams;

import java.util.ArrayList;

/**
 *
 * @author aignat
 */
public class NGramWord {

    private ArrayList<NGramYearCount> ngramYearCountList;

    public NGramWord() {
        ngramYearCountList = new ArrayList<NGramYearCount>();
    }

    public ArrayList<NGramYearCount> getNgramYearCount() {
        return ngramYearCountList;
    }
    
    public void addNgramYearCount(NGramYearCount ngramYearCount) {
        ngramYearCountList.add(ngramYearCount);
    }
    
}
