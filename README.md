# lca2011-compiler

Small proof of concept for implementing compilers on the JVM using Scala and
BCEL as presented at [LCA2011](http://lca2011.linux.org.au).

Also included is the presentation slides used at the conference.

## Status

Proof of Concept.

## Overview

A "Hello, World" compiler implemented for my presentation on compiler
construction for the JVM.

## Building

1. Edit build.properties and point the __scala.home__ property to your 
Scala installation directory.
2. Download the BCEL JAR and place it in the lib directory as "bcel.jar"
(on Ubuntu/Debian, you can install the apt package and copy it over from
/usr/share/java)
3. Run ant: $ ant

## Usage

1. Run the lcac bash script: $ ./lcac examples/Sample.lca >Program.class
2. Run the generated Java program: $ java -cp . Program

## License

This software is licensed under the terms of the [MIT License](http://en.wikipedia.org/wiki/MIT_License).

## About

lca2011-compiler was written by [Tom Lee](http://tomlee.co).

Follow me on [Twitter](http://www.twitter.com/tglee) or
[LinkedIn](http://au.linkedin.com/pub/thomas-lee/2/386/629).

