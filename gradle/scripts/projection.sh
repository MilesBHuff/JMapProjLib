#!/bin/bash

DIR=$(dirname $0)
CMD="$DIR/jmapprojlib.sh"
CLASS="com.jhlabs.map.proj.Projection"

exec "$CMD" "$CLASS" "$@"
