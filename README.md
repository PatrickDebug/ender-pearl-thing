# Carry On Ender Pearl Addon — Fabric 26.2

A server-side-only addon for Carry On.

## Intended behavior

Players already have Carry On installed normally on both client and server.

This addon only goes in the server's `mods` folder. It watches Carry On's
already-synchronized carry-key state. While the Carry On key is held, if the
player has empty hands and is aiming directly at a flying Ender Pearl, the
server calls Carry On's own `PickupHandler.tryPickupEntity(...)` method.

That means Carry On itself still handles the carried entity state and its
normal client synchronization/rendering.

## Requirements

- Minecraft Java 26.2
- Fabric Loader 0.19.3+
- Fabric API
- Carry On 2.11.x
- Java 25 to build/run Minecraft 26.2

## Build

Use a Java 25 JDK, then run:

    ./gradlew build

If you generated/opened this project without Gradle wrapper files, either:
- import it into IntelliJ with Gradle installed, or
- run `gradle wrapper`, then `./gradlew build`.

The finished jar will be under:

    build/libs/

## Installation

Put the built addon jar in the SERVER's `mods` folder only.
Players should keep their normal Carry On installation; they do not need this addon.

## Controls

Use Carry On's normal carry key with empty hands while aiming at a flying Ender Pearl.

## Notes

Carry On's own pickup method still decides whether the pickup is valid.
The addon does not replace Carry On's carry/placement system.
