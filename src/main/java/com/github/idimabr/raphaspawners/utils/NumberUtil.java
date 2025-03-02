package com.github.idimabr.raphaspawners.utils;

import java.text.DecimalFormat;

public class NumberUtil {

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#.##");
    private static final String[] currencys = new String[]{"", "K", "M", "B", "T", "Q", "QQ", "S", "SS", "O", "N", "D"};

    public static String formatValue(double value) {
        if (isInvalid(value)) return "0";

        int index = 0;

        double tmp;
        while ((tmp = value / 1000) >= 1) {
            if (index + 1 == currencys.length) break;
            value = tmp;
            ++index;
        }

        return NUMBER_FORMAT.format(value) + currencys[index];
    }

    public static boolean isInvalid(double value) {
        return value < 0 || Double.isNaN(value) || Double.isInfinite(value);
    }

}
