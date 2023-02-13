package com.intesasoft.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerHandler {
    private static Logger logger;
    private static FileHandler fileHandler;
    private static SimpleFormatter simpleFormatter = new SimpleFormatter();

    private LoggerHandler() {
    }

    public static Logger getLogger() {

        if (fileHandler == null) {
            try {
                fileHandler = new FileHandler("MyLogFile.log");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            fileHandler.setFormatter(simpleFormatter);
        }
        if (logger == null) {
            logger = Logger.getLogger("LoggerHandler");
            logger.addHandler(fileHandler);
        }
        return logger;
    }
}
