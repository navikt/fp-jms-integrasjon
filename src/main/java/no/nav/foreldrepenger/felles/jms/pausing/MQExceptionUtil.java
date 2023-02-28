package no.nav.foreldrepenger.felles.jms.pausing;

import com.ibm.mq.MQException;
import com.ibm.mq.jmqi.JmqiException;
import com.ibm.msg.client.jakarta.jms.JmsExceptionDetail;

import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;

public class MQExceptionUtil {

    private MQExceptionUtil() {

    }

    public static CharSequence extract(Exception je) {
        var buf = new StringBuilder(300);

        // Henter ut kun MQ/JMS meldinger. Resten logges som vanlig Exception med cause.
        Throwable t = je;
        while (t != null) {

            if (t instanceof JMSException je1) { // NOSONAR
                startExceptionLine(je, buf, t);
                buf.append(",JMS Errorcode=").append(je1.getErrorCode()); //$NON-NLS-1$
                if (t instanceof JmsExceptionDetail jed) { // NOSONAR
                    buf.append(",JMS Explanation=").append(jed.getExplanation()); //$NON-NLS-1$
                    buf.append(",JMS UserAction=").append(jed.getUserAction()); //$NON-NLS-1$
                }
                buf.append(';');
            } else if (t instanceof JMSRuntimeException je1) { // NOSONAR
                startExceptionLine(je, buf, t);
                buf.append(",JMS ErrorCode=").append(je1.getErrorCode()); //$NON-NLS-1$
                if (t instanceof JmsExceptionDetail jed) { // NOSONAR
                    buf.append(",JMS Explanation=").append(jed.getExplanation()); //$NON-NLS-1$
                    buf.append(",JMS UserAction=").append(jed.getUserAction()); //$NON-NLS-1$
                }
                buf.append(';');
            } else if (t instanceof MQException mqe) { // NOSONAR
                startExceptionLine(je, buf, t);
                buf.append(",WMQ CompletionCode=").append(mqe.getCompCode()); //$NON-NLS-1$
                buf.append(",WMQ ReasonCode=").append(mqe.getReason()); //$NON-NLS-1$
                buf.append(';');
            } else if (t instanceof JmqiException jmqie) { // NOSONAR
                startExceptionLine(je, buf, t);
                buf.append(",WMQ LogMessage=").append(jmqie.getWmqLogMessage()); //$NON-NLS-1$
                buf.append(",WMQ Explanation=").append(jmqie.getWmqMsgExplanation()); //$NON-NLS-1$
                buf.append(",WMQ MsgSummary=").append(jmqie.getWmqMsgSummary()); //$NON-NLS-1$
                buf.append(",WMQ MsgUserResponse=").append(jmqie.getWmqMsgUserResponse()); //$NON-NLS-1$
                buf.append(",WMQ Msg Severity=").append(jmqie.getWmqMsgSeverity()); //$NON-NLS-1$
                buf.append(';');
            }

            // Get the next cause
            t = t.getCause();
        }

        return buf;
    }

    private static void startExceptionLine(Exception je, StringBuilder buf, Throwable t) {
        buf.append("ex=").append(t.getClass().getName()).append(',').append(je.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
