package ru.nuyanzin.quizshell;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public class QuizShellOpts {
  public static final String PROPERTY_PREFIX = "quizshell.";
  private final QuizShell quizShell;
  private int numberOfTasks = 5;
  private Set<String> propertyNames;

  public QuizShellOpts(QuizShell quizShell) {
    this.quizShell = quizShell;
  }

  public void setNumberOfTasks(int numberOfTasks) {
    this.numberOfTasks = numberOfTasks;
  }

  public int getNumberOfTasks() {
    return numberOfTasks;
  }


  public void set(String key, String value) {
    set(key, value, false);
  }

  public boolean set(String key, String value, boolean quiet) {
    try {
      quizShell.getReflector().invoke(this, "set" + key, value);
      return true;
    } catch (Exception e) {

          // need to use System.err here because when bad command args
          // are passed this is called before init is done, meaning
          // that sqlline's error() output chokes because it depends
          // on properties like text coloring that can get set in
          // arbitrary order.
          System.err.println(
              Loc.getLocMessage(
                  "error-setting",
                  key,
                  e.getCause() == null ? e : e.getCause()));
        }
      return false;
  }

  Set<String> propertyNamesMixed() {
    TreeSet<String> names = new TreeSet<>();

    // get all the values from getXXX methods
    for (Method method : getClass().getDeclaredMethods()) {
      if (!method.getName().startsWith("get")) {
        continue;
      }

      if (method.getParameterTypes().length != 0) {
        continue;
      }

      String propName = deCamel(method.getName().substring(3));
      if (propName.equals("run")) {
        // Not a real property
        continue;
      }
      if (propName.equals("autosave")) {
        // Deprecated; property is now "autoSave"
        continue;
      }
      names.add(propName);
    }

    return names;
  }

  /** Converts "CamelCase" to "camelCase". */
  private static String deCamel(String s) {
    return s.substring(0, 1).toLowerCase(Locale.ROOT)
        + s.substring(1);
  }

  public Set<String> propertyNames() {
    if (propertyNames != null) {
      return propertyNames;
    }
    final TreeSet<String> set = new TreeSet<>();
    for (String s : propertyNamesMixed()) {
      set.add(s.toLowerCase(Locale.ROOT));
    }
    // properties names do not change at runtime
    // cache for further re-use
    propertyNames = Collections.unmodifiableSet(set);
    return propertyNames;
  }

  public Properties toProperties() throws IllegalAccessException, InvocationTargetException, ClassNotFoundException {
    Properties props = new Properties();

    for (String name : propertyNames()) {
      props.setProperty(PROPERTY_PREFIX + name, get(name));
    }

    return props;
  }

  public String get(String key) throws IllegalAccessException, ClassNotFoundException, InvocationTargetException {
    return String.valueOf(
        quizShell.getReflector().invoke(this, "get" + key));
  }

  public boolean hasProperty(String name) {
    try {
      return propertyNames().contains(name);
    } catch (Exception e) {
      // this should not happen
      // since property names are retrieved
      // based on available getters in this class
      quizShell.handleException(e);
      return false;
    }
  }
}
