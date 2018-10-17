package ru.nuyanzin.quizshell;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class QuizShell {

    /**
     * Maximum number of symbols of unknown command
     * to print in error handling message.
     */
    private static final int MAX_NUMBER_SYMBOLS_FOR_UNKNOWN_COMMAND = 100;

    /**
     * Flag to show should leave while cycle.
     */
    private boolean isExitRequired = false;

    /**
     * Output stream.
     */
    private final PrintStream outputStream;

    private final Reflector reflector;
    private final QuizShellOpts quizShellOpts;

    /**
     * Defined map of existing commands.
     */
    private final Map<String, CommandHandler> commandHandlerMap;

    /**
     * DrawingShell constructor could be called only from this class.
     *
     * @throws UnsupportedEncodingException if any of the specified charsets
     *                                      for print stream does not exist
     */
    private QuizShell() throws UnsupportedEncodingException {
      reflector = new Reflector(this);
      quizShellOpts = new QuizShellOpts(this);

      outputStream = new PrintStream(
          System.out, true, StandardCharsets.UTF_8.name());

      final GeneralCommands commands = new GeneralCommands(this);

      commandHandlerMap =
          Collections.unmodifiableMap(new HashMap<String, CommandHandler>() {{
            put("PLUS", new ReflectiveCommandHandler<>(
                QuizShell.this, commands, "PLUS"));
            put("+", new ReflectiveCommandHandler<>(
                QuizShell.this, commands, "PLUS"));
            put("H", new ReflectiveCommandHandler<>(
                QuizShell.this, commands, "H"));
            put("SET", new ReflectiveCommandHandler<>(
                QuizShell.this, commands, "SET"));
            put("Q", new ReflectiveCommandHandler<>(
                QuizShell.this, commands, "Q"));
          }});
    }

    /**
     * @param args startup args
     */
    public static void main(final String[] args)
        throws UnsupportedEncodingException {
      QuizShell shell = new QuizShell();

      if (args == null || args.length == 0) {
        shell.start(System.in);
      } else if (args.length == 1) {
        Path absolutePathToFile = Paths.get(args[0]).toAbsolutePath();
        if (Files.exists(absolutePathToFile)) {
          try (FileInputStream fis =
                   new FileInputStream(absolutePathToFile.toFile())) {
            shell.start(fis);
          } catch (IOException e) {
            shell.handleException(e);
          }
        } else {
          shell.output(Loc.getLocMessage("file-not-exist",
              absolutePathToFile.toString()));
        }
      } else {
        shell.output(Loc.getLocMessage("usage-start"));
      }
    }

    /**
     * Shell start.
     *
     * @param inputStream input stream to work with
     */
    private void start(final InputStream inputStream) {
      try (BufferedReader scanner = new BufferedReader(
          new InputStreamReader(inputStream, StandardCharsets.UTF_8.name()))) {
        String fullCommandLine = "";
        while (!isExitRequired && fullCommandLine != null) {
          try {
            output(getPrompt(), false);
            fullCommandLine = scanner.readLine();

            if (fullCommandLine == null) {
              output("\n" + Loc.getLocMessage("eof-detected"));
              isExitRequired = true;
              continue;
            }
            // if the input stream is not System.in then repeat the original
            // command (or cut version in case of very long line) in output.
            // Could be helpful while analysis
            // of output after working with file commands.
            if (!System.in.equals(inputStream)) {
              output(getCutString(fullCommandLine));
            }
            String trimmedLine = fullCommandLine.trim();
            if (trimmedLine.isEmpty()) {
              continue;
            }
            // currently commands are simple and do not contain spaces
            // in case the requirement change the logic should be adapted
            int firstSpaceIndex = trimmedLine.indexOf(" ");
            String commandName = trimmedLine;
            if (firstSpaceIndex != -1) {
              commandName = trimmedLine.substring(0, firstSpaceIndex);
            }
            executeCommand(fullCommandLine, trimmedLine, commandName);
          } catch (Throwable t) {
            handleException(t);
            output(getPrompt(), false);
          }
        }
      } catch (IOException e) {
        handleException(e);
      }
    }

    /**
     * Determine the right command handler and execute the command.
     *
     * @param fullCommandLine full command line
     * @param trimmedLine     trimmed command line
     * @param commandName     parsed command name
     */
    private void executeCommand(final String fullCommandLine,
                                final String trimmedLine,
                                final String commandName) {
      CommandHandler commandHandler = commandHandlerMap.get(commandName.toUpperCase(Locale.ROOT));
      if (commandHandler != null) {
        if (Objects.equals(commandName, trimmedLine)) {
          commandHandler.execute("");
        } else {
          commandHandler.execute(
              fullCommandLine.substring(
                  fullCommandLine.indexOf(commandName)
                      + commandName.length() + 1));
        }
      } else {
        output(Loc.getLocMessage("unknown-command", getCutString(commandName)));
      }
    }

    /**
     * Cut the line if its length
     * is longer then {@link #MAX_NUMBER_SYMBOLS_FOR_UNKNOWN_COMMAND}.
     *
     * @param line line to cut
     * @return the cut or original line depending on its length
     */
    public static String getCutString(String line) {
      return line.length() > MAX_NUMBER_SYMBOLS_FOR_UNKNOWN_COMMAND
          ? line.substring(0, MAX_NUMBER_SYMBOLS_FOR_UNKNOWN_COMMAND)
          + Loc.getLocMessage("rest-is-cut")
          : line;
    }

    /**
     * Prompt.
     *
     * @return prompt string.
     */
    private String getPrompt() {
      return Loc.getLocMessage("prompt");
    }

    /**
     * Exit.
     */
    public void exit() {
      isExitRequired = true;
    }

    /**
     * Print the specified message to the console and add a new line in the end.
     *
     * @param msg the message to print
     */
    public void outputWithPrompt(final String msg) {
    output(msg, true);
  }

    /**
     * Print the specified message to the console and add a new line in the end.
     *
     * @param msg the message to print
     */
    public void output(final String msg) {
      output(msg, true);
    }

    /**
     * Print the specified message to the console.
     *
     * @param msg     the message to print
     * @param newline if false, do not append a newline
     */
    public void output(final String msg, final boolean newline) {
      if (newline) {
        outputStream.println(msg);
      } else {
        outputStream.print(msg);
      }
    }

    public Reflector getReflector() {
    return reflector;
  }

    public QuizShellOpts getOpts() {
      return quizShellOpts;
    }

    /**
     * Get print stream output.
     *
     * @return the output stream
     */
    public PrintStream getOutput() {
      return outputStream;
    }

    /**
     * Exception handling.
     *
     * @param e exception/error/throwable to handle
     */
    public void handleException(Throwable e) {
      while (e instanceof InvocationTargetException) {
        e = ((InvocationTargetException) e).getTargetException();
      }
      e.printStackTrace(outputStream);
    }

  String[] split(String line, String delim) {
    return split(line, delim, 0);
  }

  public String[] split(String line, String delim, int limit) {
    if (delim.indexOf('\'') != -1 || delim.indexOf('"') != -1) {
      // quotes in delim are not supported yet
      throw new UnsupportedOperationException();
    }
    boolean inQuotes = false;
    int tokenStart = 0;
    int lastProcessedIndex = 0;

    List<String> tokens = new ArrayList<>();
    for (int i = 0; i < line.length(); i++) {
      if (limit > 0 && tokens.size() == limit) {
        break;
      }
      if (line.charAt(i) == '\'' || line.charAt(i) == '"') {
        if (inQuotes) {
          if (line.charAt(tokenStart) == line.charAt(i)) {
            inQuotes = false;
            tokens.add(line.substring(tokenStart, i + 1));
            lastProcessedIndex = i;
          }
        } else {
          tokenStart = i;
          inQuotes = true;
        }
      } else if (line.regionMatches(i, delim, 0, delim.length())) {
        if (inQuotes) {
          i += delim.length() - 1;
          continue;
        } else if (i > 0 && (
            !line.regionMatches(i - delim.length(), delim, 0, delim.length())
                && line.charAt(i - 1) != '\''
                && line.charAt(i - 1) != '"')) {
          tokens.add(line.substring(tokenStart, i));
          lastProcessedIndex = i;
          i += delim.length() - 1;

        }
      } else if (i > 0
          && line.regionMatches(i - delim.length(), delim, 0, delim.length())) {
        if (inQuotes) {
          continue;
        }
        tokenStart = i;
      }
    }
    if ((lastProcessedIndex != line.length() - 1
        && (limit == 0 || limit > tokens.size()))
        || (lastProcessedIndex == 0 && line.length() == 1)) {
      tokens.add(line.substring(tokenStart, line.length()));
    }
    String[] ret = new String[tokens.size()];
    for (int i = 0; i < tokens.size(); i++) {
      ret[i] = dequote(tokens.get(i));
    }

    return ret;
  }

  String dequote(String str) {
    if (str == null) {
      return null;
    }

    if ((str.length() == 1 && (str.charAt(0) == '\'' || str.charAt(0) == '\"'))
        || ((str.charAt(0) == '"' || str.charAt(0) == '\''
        || str.charAt(str.length() - 1) == '"'
        || str.charAt(str.length() - 1) == '\'')
        && str.charAt(0) != str.charAt(str.length() - 1))) {
      throw new IllegalArgumentException(
          "A quote should be closed for <" + str + ">");
    }
    char prevQuote = 0;
    int index = 0;
    while ((str.charAt(index) == str.charAt(str.length() - index - 1))
        && (str.charAt(index) == '"' || str.charAt(index) == '\'')) {
      // if start and end point to the same element
      if (index == str.length() - index - 1) {
        if (prevQuote == str.charAt(index)) {
          throw new IllegalArgumentException(
              "A non-paired quote may not occur between the same quotes");
        } else {
          break;
        }
        // else if start and end point to neighbour elements
      } else if (index == str.length() - index - 2) {
        index++;
        break;
      }
      prevQuote = str.charAt(index);
      index++;
    }

    return index == 0 ? str : str.substring(index, str.length() - index);
  }

  /**
   * Splits the line into an array, tokenizing on space characters.
   *
   * @param line the line to break up
   * @return an array of individual words
   */
  String[] split(String line) {
    return split(line, 0);
  }

  /**
   * Splits the line into an array, tokenizing on space characters,
   * limiting the number of words to read.
   *
   * @param line the line to break up
   * @param limit the limit for number of tokens
   *        to be processed (0 means no limit)
   * @return an array of individual words
   */
  String[] split(String line, int limit) {
    return split(line, " ", limit);
  }

  void outputProperty(String key, String value) {
    output(key + " " + value);
  }
}
