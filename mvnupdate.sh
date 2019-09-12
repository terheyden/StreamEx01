#!/usr/bin/env bash

### DEP VERSIONS:

cp pom.xml pom.xml.orig
mvn versions:display-dependency-updates
mvn versions:use-latest-releases

### PROPERTY VERSIONS:

mvn versions:display-property-updates
mvn versions:update-properties

### PARENT VERSION:

mvn versions:display-parent-updates
mvn versions:update-parent

### PLUGIN VERSIONS:

mvn versions:display-plugin-updates

echo
echo "I can't auto-update plugin versions, so you'll have to do it by hand."
echo "I backed up your original POM as: pom.xml.orig"
echo
echo "When you're ready, you'll probably want to: mvn clean verify"
echo

if [[ -e "pom.xml.versionsBackup" ]]; then
    rm pom.xml.versionsBackup
fi
