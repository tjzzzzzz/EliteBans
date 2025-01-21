package fi.tj88888.eliteBans.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(timestamp));
    }

    public static String formatDatePretty(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy HH:mm");
        return sdf.format(new Date(timestamp));
    }
}