package ru.nuyanzin.quizshell;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Properties text retriever from resource.
 *
 */
public final class Loc {

  /**
   * Resource to use.
   */
  private static final ResourceBundle RESOURCE_BUNDLE =
      ResourceBundle.getBundle("QuizShell");

  /**
   * No need to have constructor public.
   */
  private Loc() {
  }

  /**
   * Retrieve the message and substitute params if specified.
   *
   * @param key    key in resource file
   * @param params params to substitute
   * @return the message with substituted params
   */
  public static String getLocMessage(final String key, final Object... params) {
    return MessageFormat.format(RESOURCE_BUNDLE.getString(key), params);
  }
}
