/**
 * Utility
 * @author take.iwiw
 * @version 1.0.0
 */
package com.take_iwiw.tonguetwisterteacher;


public class Utility {
    static String BR = System.getProperty("line.separator");

    static Float JUDGE_SIMILAR_THRESHOLD[] = {0.9f, 0.8f, 0.7f};

    /* Convert time[sec] to "00:00.00" */
    static public String convertTimeFormat(Float time) {
        Integer timeInt = time.intValue();
        String str = String.format("%02d:%02d.%02d", (timeInt/60)%60, timeInt%60, (int)((time - timeInt) * 100));
        return str;
    }

    /* Examine 2 sentences distance */
    /* Return score (0.0 = different, 1.0 = same) */
    static public Float checkSimilar(String str1, String str2) {
        if (str1 == null || str1.trim().isEmpty()) {
            if (str2 == null || str2.trim().isEmpty()) {
                return 1.0f;
            } else {
                return 0.0f;
            }

        } else if (str2 == null || str2.trim().isEmpty()) {
            return 0.0f;
        }
        str1 = eliminatePunctuation(str1);
        str2 = eliminatePunctuation(str2);

        int distance = computeLevenshteinDistance(str1, str2);
        return 1.0f - ((float) distance / Math.max(str2.length(), str1.length()));
    }

    static private String eliminatePunctuation (String str) {
        str = str.toLowerCase();
        str = str.replace(" ", "");
        str = str.replace("\r", "");
        str = str.replace("\n", "");
        str = str.replace(".", "");
        str = str.replace(",", "");
        str = str.replace("!", "");
        str = str.replace("?", "");
        return str;
    }

    /* refer: http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java */
    private static int computeLevenshteinDistance(String str1,String str2) {
        int[][] distance = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++)
            distance[i][0] = i;
        for (int j = 1; j <= str2.length(); j++)
            distance[0][j] = j;

        for (int i = 1; i <= str1.length(); i++)
            for (int j = 1; j <= str2.length(); j++)
                distance[i][j] = minimum(
                        distance[i - 1][j] + 1,
                        distance[i][j - 1] + 1,
                        distance[i - 1][j - 1] + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1));

        return distance[str1.length()][str2.length()];
    }

    private static int minimum(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }



}
