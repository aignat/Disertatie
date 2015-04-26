
package ngrams;

/**
 *
 * @author aignat
 */
public class NGramYearCount {
    
    private String word;
    private int year;
    private int count;

    public NGramYearCount(String word, int year, int count) {
        this.word = word;
        this.year = year;
        this.count = count;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }        
    
}
