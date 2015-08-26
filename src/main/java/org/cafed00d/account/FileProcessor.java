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
    String fileName = infile.getAbsolutePath();
    log.info("processing file: " + fileName);
    String backupFileName = generateFileName(infile, ".bak");
    log.info("backing up as: " + backupFileName);
    File backupFile = new File(backupFileName);
    if (backupFile.exists()) {
      backupFile.delete();
    }
    if (!infile.renameTo(new File(backupFileName))) {
      Reporter.INSTANCE.displayError("Unable to rename file " + fileName + " to " + backupFileName);
    } else {
      Reporter.INSTANCE.displayMessage("Correcting " + fileName);
      outfile = new File(fileName);
      infile = new File(backupFileName);
      try {
        processContents();
      } catch (Exception e) {
        Reporter.INSTANCE.displayError("Error while processing file " + fileName + ": " + e.getMessage());
        log.error(e.getMessage(), e);
      }
      reportStatistics();
    }
  }

  /**
   * Generates an absolute file name with the given extension using the given
   * file name as the base. Follows these conventions:
   * <ul>
   * <li>If the simple file name contains no dots, appends
   * <code>extension</code></li>
   * <li>If the simple file name contains a dot only as the first character (ex:
   * .log), appends <code>extension</code></li>
   * <li>If the simple file names has a name part and an extension part (the
   * extension part is the part after the last dot), then it replaces the
   * existing extension with <code>extension</code></li>
   * </ul>
   * <p>
   * Note the assumption that file names will not end in a dot.
   * 
   * @param file
   *          The file for which to generate a name.
   * @param extension
   *          The extension to use for the name file name.
   * @return The generate absolute path name.
   */
  private String generateFileName(File file, String extension) {
    StringBuilder backupName = new StringBuilder(file.getName());
    int inx = backupName.toString().lastIndexOf('.');
    if (inx > 0) {
      backupName.setLength(inx);
    }
    backupName.append(extension);
    String fullName = file.getParent() + File.separator + backupName.toString();
    return fullName;
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
