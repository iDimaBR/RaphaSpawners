package com.github.idimabr.raphaspawners.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberUtil {

    private static String format(double value) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###", new DecimalFormatSymbols(new Locale("pt", "BR")));
        return decimalFormat.format(value);
    }

    public static String getFormat(double value) {
        String[] simbols = new String[]{"", "K", "M", "B", "T", "Q", "QQ", "S", "SS", "O", "N", "D"};
        int index;
        for (index = 0; value / 1000.0 >= 1.0; value /= 1000.0, ++index) {
        }
        return format(value) + simbols[index];
    }
    public static String formatValue(float value) {
        String[] arr = {"", "K", "M", "B", "T", "Q", "QQ", "S", "SS", "O", "N", "D"};
        int index = 0;
        while ((value / 1000) >= 1) {
            value = value / 1000;
            index++;
        }
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return String.format("%s%s", decimalFormat.format(value), arr[index]);
    }

}
