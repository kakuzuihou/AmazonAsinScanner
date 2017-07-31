# AmazonAsinScanner
This is a spider for scanning the asin codes from Amazon!
When you want to get the asin code from the search results that you searched in Amazon, then you can use it.

First you should search your keywords in Amazon, you will get the search url form the address bar of your brower.
Example:
https://www.amazon.co.jp/s/ref=nb_sb_noss_1?__mk_ja_JP=%E3%82%AB%E3%82%BF%E3%82%AB%E3%83%8A&url=search-alias%3Daps&field-keywords=python

There may be 24250 items, and too many pages to count out the ansin codes.
But you can use it, give the search url and the name of outputting csv file to it, it can output the asin codes of 24250 items to the csv file.
Example:
java -jar AsinScanner.jar <URL> <output_filename>
