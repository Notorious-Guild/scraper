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
    }
}

def appAppenders = []
appAppenders << "STDOUT"

root(logLevelByString["INFO"], appAppenders)