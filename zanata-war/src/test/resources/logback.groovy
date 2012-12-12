import ch.qos.logback.classic.boolex.GEventEvaluator
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.filter.EvaluatorFilter

import static ch.qos.logback.classic.Level.*
import static ch.qos.logback.core.spi.FilterReply.*

def patternExpression = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n%rootException"


appender("STDERR", ConsoleAppender) {
    filter(EvaluatorFilter) {
      evaluator(GEventEvaluator) {
        expression = 'e.level.toInt() >= WARN.toInt()'
      }
      onMatch = NEUTRAL
      onMismatch = DENY
    }
    encoder(PatternLayoutEncoder) {
      pattern = patternExpression
    }
    target = "System.err"
  }

appender("STDOUT", ConsoleAppender) {
    filter(EvaluatorFilter) {
      evaluator(GEventEvaluator) {
        expression = 'e.level.toInt() < WARN.toInt()'
      }
      onMismatch = DENY
      onMatch = NEUTRAL
    }
    encoder(PatternLayoutEncoder) {
      pattern = patternExpression
    }
    target = "System.out"
}

// set to TRACE to see hibernate generated sql
//logger("org.hibernate.SQL", TRACE)
//logger("org.hibernate", INFO)

root(ERROR, ["STDERR","STDOUT"])
