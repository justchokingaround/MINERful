#!/bin/bash

# Author:       Claudio Di Ciccio
# Date:         2026/03/05
# Description:  This script launches the MinerFulDiagnosis, in order to diagnose where in a passed event log the constraints of a given specification got satisfied or violated.
#               Run this launcher with "-h" to explore the options you can pass.
# Installation: Please download the following files and directories from the MINERful GitHub Repository (https://github.com/cdc08x/MINERful):
#                   bin/
#                   lib/
#                   src/
#                   libs.cfg


## Exec-specific parameters
DEBUGLEVEL="none"
MEMORY_MAX="16G"
THRESHOLD=1.0

## Preliminary checks
if [ ! -f ./libs.cfg ]
then
 echo "Please download the file named libs.cfg from the GitHub repository"
 exit 1
fi

## Import the libraries and store it into $LIBS
. ./libs.cfg

## Clean up the screen
clear

## Runtime environment constants
MAINCLASS="minerful.MinerFulDiagnosisStarter"

## Run!
java -Xmx$MEMORY_MAX -cp MINERful.jar:$LIBS $MAINCLASS $* # -d $DEBUGLEVEL
