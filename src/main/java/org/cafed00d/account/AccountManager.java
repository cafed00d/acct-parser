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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages information about the bank accounts that are of interest.
 * <p>
 * Not all bank transaction are of interest, only specific ones. This class
 * reads the <code>account.properties</code> file to determine how to recognize
 * accounts of interest based on contents of the description field within the
 * transaction.
 */
public enum AccountManager {

                            /**
                             * The singleton instance
                             */
  INSTANCE;

  /**
   * Logger for this class.
   */
  private final Logger log = LogManager.getLogger(AccountManager.class);

  /**
   * The name of the properties file. The file must be in the classpath, and the
   * name is relative to the classpath.
   */
  public static final String PROP_FILE = "account.properties";

  /**
   * Stores the account information.
   * <p>
   * Key is the simple account name used with R.
   * <p>
   * Value is a substring of the transaction description that can be used to
   * identify the account.
   */
  private Properties props = new Properties();

  /**
   * Loads the contents of the <code>account.properties</code> file.
   */
  private AccountManager() {
    try (InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(PROP_FILE)) {
      props.load(stream);
      log.debug("Read properties from " + PROP_FILE + ":");
      for (Entry<Object, Object> prop : props.entrySet()) {
        log.debug("  " + prop.getKey() + " = " + prop.getValue());
      }
    } catch (IOException e) {
      log.fatal("Unable to read account properties file", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Determines if the transaction description text passed is for an account of
   * interest.
   * 
   * @param description
   *          The description that came from the bank.
   * @return True if of interest, false if not.
   */
  public boolean isRecognizedAccount(String description) {
    return convertAccount(description) != null;
  }

  /**
   * Converts the transaction description passed into a simple account name that
   * will be used in the output CSV file.
   * 
   * @param description
   *          The description that came from the bank.
   * @return A simple account name suitable for use with R. Will be null if the
   *         description is not for a recognized account.
   */
  public String convertAccount(String description) {
    String result = null;
    log.debug("convertAccount input: " + description);
    for (Entry<Object, Object> prop : props.entrySet()) {
      if (description.contains(prop.getValue().toString())) {
        result = prop.getKey().toString();
        log.debug("found match with account: " + result);
        break;
      }
    }
    return result;
  }

}
