package main;

import math.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aignat on 5/14/2015.
 */
public class PeaksFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeaksFinder.class);

    public static void main(String[] args) {

        //write peak years for all WordNet words to Constants.WORDNET_WORDS_PEAKYEARS_FILE
//        try {
//            NGramUtils.writePeaksForAllWordNetWordsToFile();
//        } catch (CustomException e) {
//            LOGGER.error(e.getMessage() + ":" + e.getOriginatingMethodName());
//        }

        List<Float> data = new ArrayList<Float>();
        data.add(2F);
        data.add(4F);
        data.add(4F);
        data.add(4F);
        data.add(5F);
        data.add(5F);
        data.add(7F);
        data.add(9F);
        System.out.println(MathUtils.getStandardDeviation(data));

        System.out.println("Done finding peaks");
    }
}
