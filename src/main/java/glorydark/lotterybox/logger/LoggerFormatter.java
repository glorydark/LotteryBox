package glorydark.lotterybox.logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author glorydark
 */
public class LoggerFormatter extends Formatter {
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");

    public synchronized String format(LogRecord record) {
        return format.format(new Date(record.getMillis())) + ": " + formatMessage(record) + "\n";
    }
}