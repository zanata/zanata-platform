#!/bin/bash

# run this script under gwt-editor so that it will start up code-server hot deployment.
# once it starts, go to the url it prints out in console and add the bookmarklet to your browser to trigger gwt rebuilt.
# with this you don't need to redeploy your app if you just change gwt client side code.
# at the moment only chrome module is enabled with this so you need to use chrome.

mvn clean org.codehaus.mojo:gwt-maven-plugin:run-codeserver -Dchrome

