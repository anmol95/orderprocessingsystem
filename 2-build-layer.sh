#!/bin/bash
set -eo pipefail
gradle -q packageLibs
mv build/distributions/orderprocessingsystem.zip build/orderprocessingsystem-lib.zip