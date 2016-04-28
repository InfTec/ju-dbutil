// Copied from https://bitbucket.org/bn3t/liquibase-slf4j/src/abde0696cde5912a8a7fb8ab2279c247b57089ac/src/main/java/liquibase/ext/slf4jlogging/Slf4jLogger.java?at=default
// Provides a Logger implementation for the Liquibase logging extension framework that will log
// to a Slf4J logger instead of stderr.

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package liquibase.ext.slf4jlogging;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.logging.LogLevel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jLogger implements liquibase.logging.Logger {
    private String name = "liquibase";
    Logger logger = LoggerFactory.getLogger(name);

    public Slf4jLogger() {
    }

    public int getPriority() {
        return 5;
    }

    public void setName(String name) {
        logger = LoggerFactory.getLogger(name);
    }

    public void setLogLevel(String logLevel, String logFile) {
        // ignore
    }

    public void severe(String message) {
        logger.error(message);
    }

    public void severe(String message, Throwable e) {
        logger.error(message, e);
    }

    public void warning(String message) {
        logger.warn(message);
    }

    public void warning(String message, Throwable e) {
        logger.warn(message, e);
    }

    public void info(String message) {
        logger.info(message);
    }

    public void info(String message, Throwable e) {
        logger.info(message, e);
    }

    public void debug(String message) {
        logger.debug(message);
    }

    public void debug(String message, Throwable e) {
        logger.debug(message, e);
    }

    public void setLogLevel(String level) {
    }

    public void setLogLevel(LogLevel level) {
    }

    public LogLevel getLogLevel() {
            return null;
    }
    
    @Override
    public void setChangeLog(DatabaseChangeLog databaseChangeLog) {
    }
    
    @Override
    public void setChangeSet(ChangeSet changeSet) {
    }
}