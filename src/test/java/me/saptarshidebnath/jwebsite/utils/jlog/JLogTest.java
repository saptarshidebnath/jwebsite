package me.saptarshidebnath.jwebsite.utils.jlog;

import org.junit.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.StreamHandler;

import static me.saptarshidebnath.jwebsite.utils.Constants.DEFAULT_STACK_TRACE_CLASS_NAME_DEPTH;
import static me.saptarshidebnath.jwebsite.utils.Constants.REPLACEMENT_LEVEL_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.hamcrest.core.StringStartsWith.startsWith;

/**
 * Created by Saptarshi on 8/14/2016.
 */
public class JLogTest {
    private static String testMessage = null;
    private static String loggedMessageRegexWihtoutException = null;
    private static StreamHandler testHandler = null;
    private static ByteArrayOutputStream logHandlerBAOS = null;
    private static String loggedMessageStackTraceRegex = null;
    private static String exceptionMessage = null;

    @org.junit.BeforeClass
    public static void setUp() throws Exception {
        Class.forName("me.saptarshidebnath.jwebsite.utils.jlog.JLog");
        JLogTest.testMessage = "Test Message " + new Date();
        JLogTest.loggedMessageRegexWihtoutException = "^\\[\\s{1}" + REPLACEMENT_LEVEL_VALUE + "\\s{1}\\]\\s{1}-\\s{1}\\[\\s{1}([A-Z]{1}[a-z]{2}\\s{1}){2}[0-9]{2}\\s{1}([0-9]{2}:){2}[0-9]{2}\\s{1}[A-Z]{3}\\s{1}[0-9]{4}\\s{1}\\]\\s{1}-\\s\\[.*,\\s[0-9]+\\s\\]\\s>>\\s" + JLogTest.testMessage + "$";
        JLogTest.loggedMessageStackTraceRegex = "^\\s+at\\s{1}[a-zA-Z]+(\\.[a-zA-Z0-9\\-\\$]+)+\\(.*\\)$";
        JLogTest.exceptionMessage = "Test Message " + new Date();
    }

    @org.junit.AfterClass
    public static void tearDown() throws Exception {

    }

    @org.junit.Before
    public void beforeTest() {
        //
        // Refresh the Log handler
        //
        JLogTest.logHandlerBAOS = new ByteArrayOutputStream();

        JLogConsoleFormatter formatter = new JLogConsoleFormatter();
        formatter.setDebug(false);
        formatter.setStackTraceDepth(DEFAULT_STACK_TRACE_CLASS_NAME_DEPTH - 1);
        testHandler = new StreamHandler(new PrintStream(JLogTest.logHandlerBAOS, true), formatter);
        JLog.addHandler(testHandler);
    }

    @org.junit.After
    public void afterTest() {
        JLog.removeHandler(testHandler);
        testHandler.close();
    }

    @org.junit.Test
    public void getHandlers() {
        Handler[] handlersList = JLog.getHandlers();
        assertThat("Checking the count of the handler attached to the log : ", handlersList.length >= 1);
        assertThat("Checking if our hnadler is attached to the logger or not : ", handlersList, hasItemInArray(testHandler));
    }

    @org.junit.Test
    public void logWithoutException() throws IOException {
        JLog.log(Level.INFO.INFO, testMessage);
        this.testLoggerWithoutExceptionWith(Level.INFO, this.testMessage);
    }

    @org.junit.Test
    public void logWithException() throws IOException {
        JLog.log(Level.INFO, JLogTest.testMessage, new RuntimeException(exceptionMessage));
        this.testLoggerWithExceptionWith(Level.INFO, this.testMessage);
    }

    @org.junit.Test
    public void infoWithException() throws Exception {
        JLog.info(JLogTest.testMessage, new RuntimeException(exceptionMessage));
        this.testLoggerWithExceptionWith(Level.INFO, this.testMessage);
    }

    @org.junit.Test
    public void infoWithoutException() throws Exception {
        JLog.info(testMessage);
        this.testLoggerWithoutExceptionWith(Level.INFO, this.testMessage);
    }

    @org.junit.Test
    public void severeWithoutException() throws Exception {
        JLog.severe(testMessage);
        this.testLoggerWithoutExceptionWith(Level.SEVERE, this.testMessage);
    }

    @org.junit.Test
    public void severeWithException() throws Exception {
        JLog.severe(JLogTest.testMessage, new RuntimeException(exceptionMessage));
        this.testLoggerWithExceptionWith(Level.SEVERE, this.testMessage);
    }

    @org.junit.Test
    public void warningWithOutException() throws IOException {
        JLog.warning(testMessage);
        this.testLoggerWithoutExceptionWith(Level.WARNING, this.testMessage);
    }

    @org.junit.Test
    public void warningWithException() throws IOException {
        JLog.warning(JLogTest.testMessage, new RuntimeException(exceptionMessage));
        this.testLoggerWithExceptionWith(Level.WARNING, this.testMessage);
    }


    private void testLoggerWithoutExceptionWith(Level level, String testMessage) throws IOException {
        String loggedMessage = getCapturedLog();
        System.out.println("Log received :-");
        System.out.println(loggedMessage);
        System.out.println();
        assertThat("Checking log without exception > log end : ", loggedMessage, endsWith(testMessage));
        assertThat("Checking log without exception > start : ", loggedMessage, startsWith("[ " + level.getLocalizedName() + " ]"));
        Assert.assertTrue("Checking log without exception > with regex to match the generic pattern : ", loggedMessage.matches(this.loggedMessageRegexWihtoutException.replaceAll(REPLACEMENT_LEVEL_VALUE, level.getLocalizedName())));
    }


    private void testLoggerWithExceptionWith(Level level, String testMessage) throws IOException {
        String[] loggedMessage = getCapturedLog().split(System.lineSeparator());
        String firstLine = loggedMessage[0];
        assertThat("Checking log with exception > first line for exception message", firstLine, endsWith(exceptionMessage));
        assertThat("Checking log with exception > Number of line count should be greater than 2", loggedMessage.length, greaterThan(2));
        for (int i = 2; i < loggedMessage.length; i++) {
            Assert.assertTrue("Checking log with exception > checking for existence of stacktrace [" + i + "] :", loggedMessage[i].matches(JLogTest.loggedMessageStackTraceRegex));
        }
    }

    public String getCapturedLog() throws IOException {
        JLogTest.testHandler.flush();
        return JLogTest.logHandlerBAOS.toString().trim();
    }
}