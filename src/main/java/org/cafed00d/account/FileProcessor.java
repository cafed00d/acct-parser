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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Processes a CSV file that came from the bank and converts it into a file
 * easier to process using R.
 */
public class FileProcessor {

  /**
   * Logger for this class.
   */
  private static final Logger log = LogManager.getLogger(Parse.class);

  /**
   * The file being processed.
   */
  private File infile;

  /**
   * The file containing the data to be processed using R.
   */
  private File outfile;

  /**
   * Records the number of input lines processed.
   */
  private int inLineCount;

  /**
   * Records the number of output lines generated.
   */
  private int outLineCount;

  /**
   * Constructor
   * 
   * @param file
   *          The CSV file to process.
   */
  public FileProcessor(File file) {
    this.infile = file.getAbsoluteFile();
  }

  /**
   * Processes the file by:
   * <ol>
   * <li>Backing up the file via a rename</li>
   * <li>Recreating the file using a different CSV format for use in R</li>
   * </ol>
   */
  public void process() {
    String inFileName = infile.getAbsolutePath();
    log.info("processing file: " + inFileName);
    String outFileName = generateFileName(infile);
    log.info("creating file: " + outFileName);
    outfile = new File(outFileName);
    if (outfile.exists()) {
      outfile.delete();
    }
    Reporter.INSTANCE.displayMessage("Processing " + inFileName);
    try {
      processContents();
    } catch (Exception e) {
      Reporter.INSTANCE.displayError("Error while processing file " + inFileName + ": " + e.getMessage());
      log.error(e.getMessage(), e);
    }
    Reporter.INSTANCE.displayMessage("Created file " + outFileName);
    reportStatistics();
  }

  /**
   * Suffix added to the input file name, just before the extension.
   */
  private static final String SUFFIX = "_r";

  /**
   * Generates an absolute file name using the given file name as the base. The
   * resulting filename name "_r" inserted before the extension.
   * 
   * @param file
   *          The file for which to generate a name.
   * @return The generated absolute path name of the new file.
   */
  private String generateFileName(File file) {
    StringBuilder builder = new StringBuilder(file.getAbsolutePath().length() + 2);
    builder.append(file.getParentFile().getAbsolutePath()).append(File.separatorChar);
    String name = file.getName();
    int inx = name.lastIndexOf(".");
    if (inx < 0) {
      // No extension on file name, simply append the suffix
      builder.append(name).append(SUFFIX);
    } else {
      // There is an extension, insert suffix just before it.
      builder.append(name.substring(0, inx)).append(SUFFIX).append(name.substring(inx));
    }
    return builder.toString();
  }

  /**
   * Processes the contents of the backup file (the renamed input file), copying
   * only the desired records to the newly-created CSV file with the original
   * file's name. Calls {@link #processLine(String)} for each line.
   * 
   * @throws Exception
   *           Something went wrong.
   */
  private void processContents() throws Exception {
    try (PrintStream out = new PrintStream(new FileOutputStream(outfile));
        BufferedReader in = new BufferedReader(new FileReader(infile))) {
      printHeader(out);
      String inLine = null;
      while ((inLine = in.readLine()) != null) {
        inLineCount++;
        log.debug("***Line #" + inLineCount + ": " + inLine);
        try {
          String outLine = processLine(inLine);
          if (outLine == null) {
            log.debug("       <<ignored>>");
          } else {
            log.debug("       >>" + outLine);
            out.println(outLine);
            outLineCount++;
          }
        } catch (Exception e) {
          Reporter.INSTANCE.displayError("Error encountered processing line #" + inLineCount + ": " + inLine);
          log.error(e.getMessage(), e);
        }
      }
    }
  }

  /**
   * Separator used for CSV files.
   */
  public static final String COMMA = ",";

  /**
   * Prints the column headers for the output CSV file
   * 
   * @param out
   *          The output file.
   */
  private void printHeader(PrintStream out) {
    out.println("year,month,day,amount,account");
  }

  /**
   * Examines the CSV line given looking for accounts that are of interest. Does
   * this by using the Account class which maintains information about how to
   * recognize each account of interest. If the account is of interest, converts
   * the CSV line into another CSV line that will be easier to use in R to
   * generate the desired reports and graphs.
   * 
   * @param line
   *          The CSV line to process.
   * @return A different CSV line if the account for the input line is of
   *         interest, or null of the input line is not of interest.
   */
  private String processLine(String line) {
    StringBuilder result = new StringBuilder();
    Transaction trans = Transaction.parse(line);
    if (trans != null) {
      if (AccountManager.INSTANCE.isRecognizedAccount(trans.getDescription())) {
        result.append(trans.getYear()).append(COMMA);
        result.append(trans.getMonth()).append(COMMA);
        result.append(trans.getDay()).append(COMMA);
        result.append(trans.getAmount()).append(COMMA);
        result.append(AccountManager.INSTANCE.convertAccount(trans.getDescription()));
      }
    }
    return result.length() == 0 ? null : result.toString();
  }

  /**
   * Display statistics for the file that was processed.
   */
  private void reportStatistics() {
    Reporter.INSTANCE.displayMessage("# Input Lines:  " + inLineCount);
    Reporter.INSTANCE.displayMessage("# Output Lines: " + outLineCount);
  }
}
