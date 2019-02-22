package ru.nuyanzin.quizshell;

import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Supplier;

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
    INTEGER_INSTANCE = NumberFormat.getIntegerInstance(Locale.ROOT);
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
   * @param quizShell instance of the shell.
   */
  public GeneralCommands(final QuizShell quizShell) {
    this.shell = quizShell;
  }

  /**
   * Command Plus to generate and check '+' tasks.
   *
   * @param line        full command line
   */
  public void plus(final String line) {
    String[] parts = line.trim().split(COMMAND_OPTIONS_REGEX);
    int[] args = parseIntegersOrThrow(
        Loc.getLocMessage("number-of-tasks-should-be-number"), parts);
    final int numOfTasks = getNumOfTasks(args);
    final int maxNumber = getMaxNumber(args);
    final BiFunction<Integer, Integer, Integer> result =
        (integer, integer2) -> integer + integer2;
    final BiFunction<Integer, Integer, String> output =
        (integer, integer2) -> integer + " + " + integer2 + " = ";
    Supplier<Integer> numberGenerator =
        () -> Math.abs(random.nextInt() % maxNumber);
    doTask(numOfTasks, result, output, numberGenerator);
  }

  private void doTask(int numOfTasks,
                      BiFunction<Integer, Integer, Integer> resultFunction,
                      BiFunction<Integer, Integer, String> output,
                      Supplier<Integer> numberGenerator) {
    Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());
    for (int i = 0; i < numOfTasks; i++) {
      int first = numberGenerator.get();
      int second = numberGenerator.get();
      int userAnswer = Integer.MIN_VALUE;
      final String taskNumber = i + 1 + ") ";
      shell.output(taskNumber + output.apply(first, second), false);
      final int result = resultFunction.apply(first, second);
      do {
        String answer = scanner.nextLine();
        userAnswer = getUserAnswer(userAnswer, answer);
        if (userAnswer != result) {
          shell.output(Loc.getLocMessage("answer", answer)
              + Loc.getLocMessage("not-correct-answer"));
          shell.output(taskNumber + output.apply(first, second), false);
        } else {
          shell.output(Loc.getLocMessage("answer", answer)
              + Loc.getLocMessage("correct-answer"));
        }

      } while (userAnswer != result);
    }
  }

  /**
   * Command minus to generate and check '-' tasks.
   *
   * @param line        full command line
   */
  public void minus(final String line) {
    String[] parts = line.trim().split(COMMAND_OPTIONS_REGEX);
    int[] args = parseIntegersOrThrow(
        Loc.getLocMessage("number-of-tasks-should-be-number"), parts);
    final int numOfTasks = getNumOfTasks(args);
    final int maxNumber = getMaxNumber(args);
    final BiFunction<Integer, Integer, Integer> result =
        (integer, integer2) -> Math.abs(integer - integer2);
    final BiFunction<Integer, Integer, String> output =
        (integer, integer2) -> Math.max(integer, integer2)
            + " - " + Math.min(integer, integer2) + " = ";
    Supplier<Integer> numberGenerator =
        () -> Math.abs(random.nextInt() % maxNumber);
    doTask(numOfTasks, result, output, numberGenerator);
  }

  private int getUserAnswer(int userAnswer, String answer) {
    int[] intAnswer = parseIntegersOrThrow(
        Loc.getLocMessage("answer-should-be-number", answer), answer);
    if (intAnswer != null) {
      userAnswer = intAnswer[0];
    }
    return userAnswer;
  }

  /**
   * Command Plus to generate and check '+' tasks.
   *
   * @param line        full command line
   */
  public void set(final String line) {
    if (line == null || line.trim().equals("set")
        || line.length() == 0) {
      config(null);
      return;
    }

    final String[] parts = shell.split(line);
    if (parts.length > 3) {
      shell.output("Usage: set [all | <property name> [<value>]]");
      return;
    }

    String propertyName = parts[1].toLowerCase(Locale.ROOT);

    if ("all".equals(propertyName)) {
      config(null);
      return;
    }

    if (!shell.getOpts().hasProperty(propertyName)) {
      shell.output(Loc.getLocMessage("no-specified-prop", propertyName));
      return;
    }

    if (parts.length == 2) {
      try {
        shell.outputProperty(propertyName,
            shell.getOpts().get(propertyName));
      } catch (Exception e) {
        shell.handleException(e);
      }
    } else {
      setProperty(propertyName, parts[2], null);
    }
  }

  private void setProperty(String key, String value, String res) {
    boolean success = shell.getOpts().set(key, value, false);
    if (success) {
      if (res != null) {
        shell.output(Loc.getLocMessage(res, key, value));
      }
    } else {
      shell.output("fail to set property");
    }
  }

  public void config(String line) {
    try {
      Properties props = shell.getOpts().toProperties();
      Set<String> keys = new TreeSet<String>(((Map) props).keySet());
      for (String key : keys) {
        shell.outputProperty(
            key.substring(QuizShellOpts.PROPERTY_PREFIX.length()),
            props.getProperty(key));
      }
    } catch (Exception e) {
      shell.handleException(e);
      return;
    }
  }

  private int getNumOfTasks(int[] args) {
    return args == null || args.length < 1
        ? shell.getOpts().getNumberOfTasks()
        : args[0];
  }

  private int getMaxNumber(int[] args) {
    return args == null || args.length < 2
        ? shell.getOpts().getMaxNumber()
        : args[1];
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
