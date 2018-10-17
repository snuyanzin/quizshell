package ru.nuyanzin.quizshell;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Random;

/**
 * Class for general commands.
 */
public final class GeneralCommands implements Commands {
  /**
   * Integer instance to parse integers only.
   * It is NOT threadsafe but here there is
   * no multithreading => that is currently ok.
   */
  private static final NumberFormat INTEGER_INSTANCE;

  static {
    INTEGER_INSTANCE = NumberFormat.getIntegerInstance();
    INTEGER_INSTANCE.setParseIntegerOnly(false);
  }

  /**
   * Regex to split command line arguments.
   */
  private static final String COMMAND_OPTIONS_REGEX = "\\s+";
  /**
   * Instance of the shell.
   */
  private final QuizShell shell;

  private final Random random = new Random();
  /**
   * Constructor.
   *
   * @param springerShell instance of the shell.
   */
  public GeneralCommands(final QuizShell springerShell) {
    this.shell = springerShell;
  }

  /**
   * Command B filling area connected to (x, y) in canvas.
   *
   * @param line        full command line
   */
  public void plus(final String line) {
    String[] parts = line.trim().split(COMMAND_OPTIONS_REGEX);

    String commandBUsageMessage =
        Loc.getLocMessage(
            "usage-b", "plus", "asd", "sad");
    System.out.println("parts.length " + parts.length);
    // length 2 as 2 arguments are required
    if (parts.length != 1) {
      shell.output(commandBUsageMessage);
      return;
    }
    int[] args = parseIntegersOrThrow("asd", parts[0]);
    if (args == null) {
      // just return as exception message printed from parseIntegersOrThrow
      return;
    }
    for (int i = 0; i < args[0]; i++) {
      shell.output(random.nextInt() % 10 + " + " + random.nextInt() % 10 + " = ", false);
    }
    shell.output("test");
  }

  /**
   * Command H.
   *
   * @param line full command line.
   */
  public void h(final String line) {
    shell.output(Loc.getLocMessage("help"));
  }

  /**
   * Exit command Q.
   *
   * @param line full command line.
   */
  public void q(final String line) {
    String[] parts = line.trim().split(COMMAND_OPTIONS_REGEX);
    if (parts.length > 0 && !parts[0].isEmpty()) {
      shell.output(Loc.getLocMessage("usage-q"));
      return;
    }
    shell.exit();
  }

  /**
   * Validate if args are numbers otherwise throw NumberFormatException.
   *
   * @param args args to validate
   * @return array of parsed numbers if valid
   */
  private int[] parseIntegersOrThrow(final String failMessage,
                                     final String... args) {
    INTEGER_INSTANCE.setParseIntegerOnly(false);
    if (args == null || args.length == 0) {
      return null;
    }
    int[] result = new int[args.length];
    try {
      for (int i = 0; i < args.length; i++) {
        Number parsedNumber = INTEGER_INSTANCE.parse(args[i]);
        // {@line DecimalFormat} parses into Long or Double
        if (!(parsedNumber instanceof Long)) {
          shell.output(failMessage);
          return null;
        }
        result[i] = parsedNumber.intValue();
      }
    } catch (ParseException e) {
      shell.output(failMessage);
      return null;
    }
    return result;
  }
}
