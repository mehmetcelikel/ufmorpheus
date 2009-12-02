#!/bin/sh
rm plugin.xpi
zip -r plugin.xpi *
rm ../compiled/plugin.xpi
mv plugin.xpi ../compiled/
echo "Successfull!"
