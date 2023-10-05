#!/bin/bash

git pull

#expecting BCM2708 or BCM2709.
#If this is a genuine Raspberry Pi then please report this to projects@drogon.net.
#If this is not a Raspberry Pi then you are on your own as wiringPi is designed to support the Raspberry Pi.
#then use this in console
export JAVA_TOOL_OPTIONS=-Dpi4j.linking=dynamic

mvn spring-boot:run