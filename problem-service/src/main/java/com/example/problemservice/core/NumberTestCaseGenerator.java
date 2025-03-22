package com.example.problemservice.core;

import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Random;

public class NumberTestCaseGenerator<T extends Number> {
    private final Random random = new Random();

    public List<T[]> generate1DTestCases(
            int numberOfTestCases,
            int minArrayLength, int maxArrayLength,
            T minValue, T maxValue,
            String directoryPath,
            Class<T> clazz
    ) {
        //List<int[]> testCases = new ArrayList<>();

        for (int i = 0; i < numberOfTestCases; i++) {
            int arrayLength = getRandomNumber(minArrayLength, maxArrayLength);
            T[] testCase = (T[]) Array.newInstance(clazz, arrayLength);

            for (int j = 0; j < arrayLength; j++) {
                testCase[j] = getRandomNumber(minValue, maxValue, clazz);
            }

            try (FileWriter writer = new FileWriter(directoryPath + "/" + i + ".txt")) {
                writer.write(arrayLength + "\n");
                for (T value : testCase) {
                    writer.write(value + " ");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //testCases.add(testCase);
        }

        return null; //testCases;
    }

    public int getRandomNumber(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    private T getRandomNumber(T min, T max, Class<T> clazz) {
        if (clazz == Integer.class) {
            return clazz.cast(random.nextInt((max.intValue() - min.intValue()) + 1) + min.intValue());
        } else if (clazz == Double.class) {
            return clazz.cast(random.nextDouble() * (max.doubleValue() - min.doubleValue()) + min.doubleValue());
        } else if (clazz == Long.class) {
            return clazz.cast(random.nextLong() % (max.longValue() - min.longValue() + 1) + min.longValue());
        } else {
            throw new IllegalArgumentException("Unsupported type: " + clazz);
        }
    }

}
