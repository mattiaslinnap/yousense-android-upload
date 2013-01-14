# YouSense Upload Library for Android

yousense-android-upload is a library for:
* Logging sensor events (and any other data) offline,
* compressing and rotating log files to the SDCard to make sure the internal storage does not run out,
* Regularly uploading the log files to a server running yousense-upload-server Django app.

Besides sensor logs, it can be used to throw any data at any time into a specific directory, and have it uploaded to the server in the background.

The upload rules are configurable, for example some apps may prefer uploading only on WiFi.
