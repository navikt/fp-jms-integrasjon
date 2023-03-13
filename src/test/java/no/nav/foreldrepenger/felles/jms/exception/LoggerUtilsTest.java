package no.nav.foreldrepenger.felles.jms.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LoggerUtilsTest {

    @Test
    void removeLineBreaks() {
        var testString = "line1 \n\r line2";
        assertThat(LoggerUtils.removeLineBreaks(testString)).isEqualTo("line1  line2");
    }

    @Test
    void toStringWithoutLineBreaks() {
        var testString = "line1\r line2\n line3\n\r line4\r\n line5";
        assertThat(LoggerUtils.toStringWithoutLineBreaks(testString)).isEqualTo("line1 line2 line3 line4 line5");
    }
}
