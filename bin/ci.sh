#!/bin/bash -ex

sbt scalastyle
sbt:test scalastyle
sbt test
