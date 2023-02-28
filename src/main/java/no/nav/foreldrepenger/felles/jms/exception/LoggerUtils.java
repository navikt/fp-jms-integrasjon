package no.nav.foreldrepenger.felles.jms.exception;

public class LoggerUtils {

    private LoggerUtils() {
        // skjult
    }

    public static String removeLineBreaks(String string) {
        return string != null ? string.replaceAll("(\\r|\\n)", "") : null;
    }

    public static String toStringWithoutLineBreaks(Object object) {
        return object != null ? removeLineBreaks(object.toString()) : null;
    }
}
