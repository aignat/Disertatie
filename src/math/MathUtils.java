package math;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexandru on 08 Jun 15.
 */
public class MathUtils {

    private static List<Double> getLocalData(int currentIndex, int windowSize, List<Double> data) {
        List<Double> localData = new ArrayList<Double>();
        localData.add(data.get(currentIndex));

        for (int i = 1; i < windowSize; i++) {
            if (currentIndex - i > 0) {
                localData.add(data.get(currentIndex - i));
            }
            if (currentIndex + i < data.size()) {
                localData.add(data.get(currentIndex + i));
            }
        }

        return localData;
    }

    private static double getAverage(List<Double> data) {

        double sum = 0;

        for (Double element : data) {
            sum += element;
        }

        return sum / data.size();
    }

    public static double getLocalAverage(int currentIndex, int windowSize, List<Double> data) {

        return getAverage(getLocalData(currentIndex, windowSize, data));
    }

    private static double getStandardDeviation(List<Double> data) {

        double sum = 0;
        double average = getAverage(data);

        for (Double element : data) {
            sum += Math.pow((element - average), 2);
        }

        return Math.sqrt(sum / data.size());
    }

    public static double getLocalStandardDeviation(int currentIndex, int windowSize, List<Double> data) {

        return getStandardDeviation(getLocalData(currentIndex, windowSize, data));
    }

}
