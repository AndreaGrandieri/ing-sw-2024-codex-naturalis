package it.polimi.ingsw.logger;

/**
 * Logger class is used to log messages in the console.
 */
public final class Logger {
    /**
     * Log levels, used to specify the importance of the log message.
     */
    public enum LogLevel {
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        NONE
    }

    private static LogLevel from = LogLevel.INFO;

    // Private constructor to prevent instantiation, since static classes has no meaning to be instantiated.
    private Logger() {
    }

    public static void setLogLevel(LogLevel level) {
        from = level;
    }

    public static void logDebug(String message) {
        if (from.compareTo(LogLevel.DEBUG) <= 0) {
            log(LogLevel.DEBUG, message);
        }
    }

    public static void logInfo(String message) {
        if (from.compareTo(LogLevel.INFO) <= 0) {
            log(LogLevel.INFO, message);
        }
    }

    public static void logWarning(String message) {
        if (from.compareTo(LogLevel.WARNING) <= 0) {
            log(LogLevel.WARNING, message);
        }
    }

    public static void logError(String message) {
        if (from.compareTo(LogLevel.ERROR) <= 0) {
            log(LogLevel.ERROR, message);
        }
    }

    private static void log(LogLevel level, String message) {
        System.out.println("[" + level + "] " + message);
    }
}
