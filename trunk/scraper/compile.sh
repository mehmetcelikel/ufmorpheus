#!/bin/sh
rm plugin.xpi
rm scraper.xpi
zip -r scraper.xpi *
rm ../compiled/plugin.xpi
rm ../compiled/scraper.xpi
mv scraper.xpi ../compiled/
echo "Successfull!"
