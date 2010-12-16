#!/bin/sh
rm plugin.xpi morpheus.xpi morpheus.zip
zip -r morpheus.zip *
rm ../compiled/plugin.xpi
rm ../compiled/morpheus.xpi
mv morpheus.zip morpheus.xpi
mv morpheus.xpi ../compiled/
echo "Successfull!"
date
