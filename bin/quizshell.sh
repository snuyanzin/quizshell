#!/bin/bash
# quizshell - Script to launch drawing shell on Unix, Linux or Mac OS

BINPATH=$(dirname $0)
exec java -cp "$BINPATH/../target/*" ru.nuyanzin.quizshell.QuizShell "$@"

# End quizshell.sh
