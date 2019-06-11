package com.vitaxa.jasteambot.service.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import com.vitaxa.jasteambot.helper.IOHelper;
import org.slf4j.LoggerFactory;

import java.io.File;

public class LogManager {

    private final static String ROOT_LOGGER_NAME = "com.vitaxa.jasteambot";

    private static LogManager INSTANCE = new LogManager();

    private LogManager() {
    }

    public static LogManager getInstance() {
        return INSTANCE;
    }

    public void setLoggingLevel(Level level) {
        Logger root = (Logger) LoggerFactory.getLogger(ROOT_LOGGER_NAME);
        root.setLevel(level);
    }

    public Level getLoggerLevel() {
        final Logger logger = (Logger) LoggerFactory.getLogger(ROOT_LOGGER_NAME);
        return logger.getLevel();
    }

    public Level getLoggerLevel(String loggerName) {
        final Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
        return logger.getLevel();
    }

    public void setLoggingLevel(Level level, String loggerName) {
        Logger root = (Logger) LoggerFactory.getLogger(loggerName);
        root.setLevel(level);
    }

    public Logger createNewLogger(String loggerName, String fileName, Level level) {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        final Logger logger = context.getLogger(loggerName);

        logger.setLevel(level);

        // Don't inherit root appender
        logger.setAdditive(false);

        final ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setName(loggerName);
        consoleAppender.setWithJansi(true);

        PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
        patternLayoutEncoder.setContext(context);
        patternLayoutEncoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %highlightex(%-5level) %msg%n");
        patternLayoutEncoder.start();

        consoleAppender.setEncoder(patternLayoutEncoder);
        consoleAppender.start();

        final RollingFileAppender<ILoggingEvent> rollingFile = new RollingFileAppender<>();
        rollingFile.setContext(context);
        rollingFile.setName(loggerName);
        rollingFile.setFile(IOHelper.WORKING_DIR + File.separator + "log" + File.separator + fileName + ".log");
        rollingFile.setAppend(true);

        final TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setFileNamePattern(IOHelper.WORKING_DIR + File.separator + "log"
                + File.separator + "archived" + File.separator + "bot"
                + File.separator + fileName + ".%d{yyyy-MM-dd}.%i.log");
        rollingPolicy.setParent(rollingFile);
        rollingPolicy.setContext(context);

        final SizeAndTimeBasedFNATP<ILoggingEvent> sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP<>();
        sizeAndTimeBasedFNATP.setMaxFileSize(FileSize.valueOf("5mb"));

        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(sizeAndTimeBasedFNATP);
        rollingPolicy.start();

        final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %msg%n");
        encoder.start();

        rollingFile.setRollingPolicy(rollingPolicy);
        rollingFile.setEncoder(encoder);
        rollingFile.start();

        logger.addAppender(rollingFile);
        logger.addAppender(consoleAppender);

        return logger;
    }

    public Logger getLogger(String loggerName) {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        return context.getLogger(loggerName);
    }

}
