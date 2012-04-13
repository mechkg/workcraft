set SCRIPT_DIR=%~dp0
java -Djline.terminal=jline.UnsupportedTerminal -Djline.WindowsTerminal.directConsole=false -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m -Xmx1024M -Xss2M -jar "%SCRIPT_DIR%\sbt-launch.jar" %*
