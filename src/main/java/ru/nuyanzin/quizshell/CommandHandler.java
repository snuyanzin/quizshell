package ru.nuyanzin.quizshell;

/**
 * A generic command to be executed. Execution of the command should be
 * dispatched to the {@link #execute(String)} method.
 *
 */
public interface CommandHandler {
  /**
   * @return the command's name
   */
  String getName();

  /**
   * Executes the specified command.
   *
   * @param line The full command line to execute
   */
  void execute(String line);
}
