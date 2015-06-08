package math;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexandru on 08 Jun 15.
 */
public class MathUtils {

    public static double getAverage(List<Float> data) {

        double sum = 0;

        for (Float element : data) {
            sum += element;
        }

        return sum / data.size();
    }

    private static List<Float> getLocalData(int currentIndex, int windowSize, List<Float> data) {
        List<Float> localData = new ArrayList<Float>();
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

    public static float getLocalAverage(int currentIndex, int windowSize, List<Float> data) {

        return (float)getAverage(getLocalData(currentIndex, windowSize, data));
    }

    public static double getStandardDeviation(List<Float> data) {

        double sum = 0;
        double average = getAverage(data);

        for (Float element : data) {
            sum += Math.pow((element - average), 2);
        }

        return sum / data.size();
    }

    public static float getLocalStandardDeviation(int currentIndex, int windowSize, List<Float> data) {

        return (float)getStandardDeviation(getLocalData(currentIndex, windowSize, data));
    }

}
