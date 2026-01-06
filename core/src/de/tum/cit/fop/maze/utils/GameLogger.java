package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for centralized logging with support for log levels and file
 * output.
 * Designed to have minimal performance impact.
 */
public class GameLogger {

    public enum LogLevel {
        DEBUG(0),
        INFO(1),
        WARN(2),
        ERROR(3),
        NONE(4);

        final int value;

        LogLevel(int value) {
            this.value = value;
        }
    }

    private static LogLevel currentLogLevel = LogLevel.DEBUG; // Default to DEBUG for development
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static boolean logToFile = false;
    private static FileHandle logFile;

    /**
     * Sets the global log level.
     * 
     * @param level The minimum level to log.
     */
    public static void setLogLevel(LogLevel level) {
        currentLogLevel = level;
    }

    public static void debug(String tag, String message) {
        log(LogLevel.DEBUG, tag, message);
    }

    public static void info(String tag, String message) {
        log(LogLevel.INFO, tag, message);
    }

    public static void warn(String tag, String message) {
        log(LogLevel.WARN, tag, message);
    }

    public static void error(String tag, String message) {
        log(LogLevel.ERROR, tag, message);
    }

    public static void error(String tag, String message, Throwable exception) {
        log(LogLevel.ERROR, tag, message + "\nException: " + exception.toString());
        if (currentLogLevel.value <= LogLevel.ERROR.value) {
            exception.printStackTrace();
        }
    }

    private static void log(LogLevel level, String tag, String message) {
        if (level.value >= currentLogLevel.value) {
            String timestamp = dtf.format(LocalDateTime.now());
            String formattedMessage = String.format("[%s] [%s] [%s]: %s", timestamp, level.name(), tag, message);

            // Print to console using LibGDX logger or System.out fallback
            if (Gdx.app != null) {
                if (level == LogLevel.ERROR) {
                    Gdx.app.error(tag, formattedMessage);
                } else {
                    Gdx.app.log(tag, formattedMessage);
                }
            } else {
                // Fallback for non-GDX threads or tests
                if (level == LogLevel.ERROR) {
                    System.err.println(formattedMessage);
                } else {
                    System.out.println(formattedMessage);
                }
            }
        }
    }
}
