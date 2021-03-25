import java.nio.charset.Charset

def logLevelByString = [
        "TRACE": TRACE,
        "DEBUG": DEBUG,
        "INFO" : INFO,
        "WARN" : WARN,
        "ERROR": ERROR
].withDefault { INFO }

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
        charset = Charset.forName('UTF-8')
    }
}

def appAppenders = []
appAppenders << "STDOUT"

def logLevel = System.getenv('LOG_LEVEL') ?: 'INFO'

root(logLevelByString[logLevel], appAppenders)
