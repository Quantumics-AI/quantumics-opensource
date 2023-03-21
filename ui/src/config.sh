#!/bin/sh

config='appConfiguration = {'
config=$config'"apiUrl"':\"$1\"', '
config=$config'"redashUrl"':\"$2\"
config=$config'}'

echo $config > config.js


