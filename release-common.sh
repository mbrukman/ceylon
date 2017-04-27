#!/bin/bash

LOG_FILE=$PWD/release.log

CEYLON_RELEASE_VERSION_MAJOR=1
CEYLON_RELEASE_VERSION_MINOR=3
CEYLON_RELEASE_VERSION_RELEASE=3
CEYLON_RELEASE_VERSION_QUALIFIER=
CEYLON_RELEASE_VERSION_PREFIXED_QUALIFIER=
CEYLON_RELEASE_VERSION=$CEYLON_RELEASE_VERSION_MAJOR.$CEYLON_RELEASE_VERSION_MINOR.$CEYLON_RELEASE_VERSION_RELEASE
CEYLON_RELEASE_VERSION_OSGI_QUALIFIER=5
CEYLON_RELEASE_VERSION_NAME="Contents May Differ REL"
CEYLON_RELEASE_VERSION_JVM_BIN_MAJ=8
CEYLON_RELEASE_VERSION_JVM_BIN_MIN=1
CEYLON_RELEASE_VERSION_JS_BIN_MAJ=10
CEYLON_RELEASE_VERSION_JS_BIN_MIN=0

CEYLON_BRANCHING_TAG=${CEYLON_RELEASE_VERSION}-branching

CEYLON_NEXT_VERSION_MAJOR=1
CEYLON_NEXT_VERSION_MINOR=3
CEYLON_NEXT_VERSION_RELEASE=4
CEYLON_NEXT_VERSION_QUALIFIER=SNAPSHOT
CEYLON_NEXT_VERSION_PREFIXED_QUALIFIER=-SNAPSHOT
CEYLON_NEXT_VERSION_OSGI_QUALIFIER=4
CEYLON_NEXT_VERSION=$CEYLON_NEXT_VERSION_MAJOR.$CEYLON_NEXT_VERSION_MINOR.${CEYLON_NEXT_VERSION_RELEASE}-${CEYLON_NEXT_VERSION_QUALIFIER}
CEYLON_NEXT_VERSION_NAME="Contents May Differ NEW"

log() {
  echo $@
  echo $@ >> $LOG_FILE
}

fail() {
  log "Failed at: $@"
  exit 1
}

replace() {
  # full version "?x.y.z(-q)?"?(/*)?@CEYLON_VERSION@(*/)? in text or code
  perl -pi -e "s|(\")?\d+\.\d+\.\d+(-\w+)?(\")?((/\*)?\@CEYLON_VERSION\@(\*/)?)|\${1}$CEYLON_NEW_VERSION\3\4|" $1
  
  # version component "?x?"?/*@CEYLON_VERSION_PART@*/ in code
  perl -pi -e "s|(\")?\d+?(\")?/\*\@CEYLON_VERSION_MAJOR\@\*/|\${1}$CEYLON_NEW_VERSION_MAJOR\${2}/*\@CEYLON_VERSION_MAJOR\@*/|" $1
  perl -pi -e "s|(\")?\d+?(\")?/\*\@CEYLON_VERSION_MINOR\@\*/|\${1}$CEYLON_NEW_VERSION_MINOR\${2}/*\@CEYLON_VERSION_MINOR\@*/|" $1
  perl -pi -e "s|(\")?\d+?(\")?/\*\@CEYLON_VERSION_RELEASE\@\*/|\${1}$CEYLON_NEW_VERSION_RELEASE\${2}/*\@CEYLON_VERSION_RELEASE\@*/|" $1
  perl -pi -e "s|(\")?\w*?(\")?/\*\@CEYLON_VERSION_QUALIFIER\@\*/|\${1}$CEYLON_NEW_VERSION_QUALIFIER\${2}/*\@CEYLON_VERSION_QUALIFIER\@*/|" $1
  perl -pi -e "s|(\")?[\w-]*?(\")?/\*\@CEYLON_VERSION_PREFIXED_QUALIFIER\@\*/|\${1}$CEYLON_NEW_VERSION_PREFIXED_QUALIFIER\${2}/*\@CEYLON_VERSION_PREFIXED_QUALIFIER\@*/|" $1
  
  # version name "bla"(/*)?@CEYLON_VERSION_NAME@(*/)? in text or code
  perl -pi -e "s|\"[^\"]+\"((/\*)?\@CEYLON_VERSION_NAME\@(\*/)?)|\"$CEYLON_NEW_VERSION_NAME\"\${1}|" $1
}

