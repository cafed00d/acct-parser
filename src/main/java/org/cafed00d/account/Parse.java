/*
 *  Copyright 2015, Peter Johnson
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */
package org.cafed00d.account;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The main class for the utility that parses a CSV file containing transaction
 * for a backing account.
 * <p>
 * <b>Usage:</b> <code>parse [-&lt;options&gt;] &lt;csv-file&gt;</code>
 * <p>
 * where <code>&lt;options&gt;</code> is one of:
 * <table summary="Options table">
 * <tr>
 * <th>Option</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <th>v</th>
 * <td>Outputs additional information while processing the files. Ignored if the
 * "q" option is also present.</td>
 * </tr>
 * <tr>
 * <th>q</th>
 * <td>Suppresses program output while the files are being processed, including
 * error messages. Note that this option does not kick in until the options
 * passed are validated, thus error messages regarding invalid command line
 * options will be displayed regardless of this setting.</td>
 * </tr>
 * </table>
 * <p>
 * The utility accepts one <code>*.csv</code> file and processes it. It first
 * backs up the <code>*.csv</code> file as <code>*.bak</code> and then creates a
 * new <code>.scv</code> file containing only entries for accounts of interest
 * using a different format easier to use within R.
 * <p>
 * <b>Example</b>
 * 
 * <pre>
 * parse c:/users/john/desktop/stmt.scv
 * </pre>
 * <p>
 * Parses the <code>stmt.scv</code> file on John's desktop so that it contains
 * only information of interest, and backs up the original file as
 * <code>stmt.bak</code> on John's desktop.
 */
public class Parse {

  /**
   * Logger for this class.
   */
  private static final Logger log = LogManager.getLogger(Parse.class);

  /**
   * If true, runs in quiet mode. Set if <code>-q</code> option passed.
   */
  private boolean quietMode = false;

  /**
   * If true, runs in verbose mode. Set if <code>-v</code> option passed.
   */
  private boolean verboseMode = false;

  /**
   * The file to process.
   */
  private File file;

  /**
   * Main body of program.
   * 
   * @param args
   *          The arguments passed by the user. See the class comment for
   *          allowed values.
   */
  public static void main(String[] args) {
    Parse ac = new Parse();
    if (ac.validate(args)) {
      ac.process();
    } else {
      displayUsage();
    }
  }

  /**
   * Displays usage instructions to the user.
   */
  private static void displayUsage() {
    Reporter.INSTANCE.displayMessage("Usage: parse [-qv] csv-file");
    Reporter.INSTANCE.displayMessage("Where:");
    Reporter.INSTANCE.displayMessage("  -q  Run in quite mode: suppress all output");
    Reporter.INSTANCE.displayMessage("  -v  Run in verbose mode: output additonal info");
    Reporter.INSTANCE.displayMessage("  csv-file CSV file containing account transactions");
  }

  /**
   * Validates the arguments that were passed. Examines the command options to
   * see which ones were selected. Verifies that a file was passed and that it
   * is writable.
   * <p>
   * This method does not care what order the options and files appear. Every
   * argument that starts with a dash (-) must contains only valid option
   * letters, and any other arguments must be writable files.
   * 
   * @param args
   *          The arguments that were passed to the utility.
   * @return True if the arguments are acceptable. False if an invalid option
   *         was passed (thereby allowing -? to be passed to get the usage) or
   *         no writable file given.
   */
  private boolean validate(String[] args) {
    boolean result = true;
    if (args.length > 0) {
      for (String arg : args) {
        if (arg.startsWith("-")) {
          if (!validateOptions(arg)) {
            result = false;
          }
        } else if (!validateFile(arg)) {
          result = false;
        }
      }
    }

    /*
     * If there were no errors configure the reporter.
     */
    if (result) {
      Reporter.setOptions(verboseMode, quietMode);
    }

    return result;
  }

  /**
   * Validates the options given.
   * 
   * @param arg
   *          An argument containing options to validate.
   * @return True iff the options are acceptable.
   */
  private boolean validateOptions(String arg) {
    log.debug("options=" + arg);
    boolean result = true;
    if (arg.length() == 0) {
      result = false;
      Reporter.INSTANCE.displayError("no options given in argument " + arg);
    } else {
      char[] chars = arg.toCharArray();
      for (char c : chars) {
        switch (c) {
          case 'v':
          case 'V':
            verboseMode = true;
            log.debug("found vebose option");
            break;

          case 'q':
          case 'Q':
            quietMode = true;
            log.debug("found quiet option");
            break;

          default:
            Reporter.INSTANCE.displayError("unknown option " + c + " in argument " + arg);
            result = false;
            break;
        }
      }
    }
    return result;
  }

  /**
   * Validates the file given.
   * 
   * @param arg
   *          An argument containing a file name to validate.
   * @return True iff the named file exists and is writable.
   */
  private boolean validateFile(String arg) {
    boolean result = false;
    file = new File(arg).getAbsoluteFile();
    if (file.exists()) {
      if (file.isFile()) {
        if (file.canWrite()) {
          result = true;
        } else {
          Reporter.INSTANCE.displayError("file is not writeable: " + file.getAbsolutePath());
        }
      } else {
        Reporter.INSTANCE.displayError("cannot convert a directory: " + file.getAbsolutePath());
      }
    } else {
      Reporter.INSTANCE.displayError("no such file: " + file.getAbsolutePath());
    }
    return result;
  }

  /**
   * Processes the CSV file.
   */
  private void process() {
    new FileProcessor(file).process();
  }
}
