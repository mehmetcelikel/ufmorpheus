#!/bin/sh
rm plugin.xpi scraper.xpi scraper.zip
zip -r scraper.zip *
rm ../compiled/plugin.xpi
rm ../compiled/scraper.xpi
mv scraper.zip scraper.xpi
mv scraper.xpi ../compiled/
echo "Successfull!"
date
