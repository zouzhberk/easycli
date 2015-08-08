#!/bin/bash

CURDIR="$(cd `dirname $0`/.; pwd)"

exec $CURDIR/cli-class com.github.zouzhberk.cli.EasyParser $@
