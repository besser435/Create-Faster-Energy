# OpenPaC-Bluemap

## Todo
* Make creative tab

## Releasing notes
When releasing jars, do not include the dev stuff. See Create's docs about that here:
Be sure to depend on Create, and crash if it's not present.
https://wiki.createmod.net/developers/depend-on-create/neoforge-1.21.1#development-environment-dependency

## Gradle Notes:
Datagen must be run to include things like block properties. This is done with `Gradle > mod development > runData`.
Make sure this completes without any errors.

To build a jar file, run the task with `Gradle > build > jar`.
