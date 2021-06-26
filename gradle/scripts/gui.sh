#!/bin/bash

DIR=$(dirname $0)
CMD="$DIR/jmapprojlib.sh"
CLASS="ch.ethz.karto.gui.Main"

exec "$CMD" "$CLASS" "$@"
