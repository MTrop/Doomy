# Doomy

Copyright (c) 2019 Matt Tropiano  

### Required Libraries

[Black Rook SQL](https://github.com/BlackRookSoftware/SQL) 1.0.0+  
[Black Rook JSON](https://github.com/BlackRookSoftware/JSON) 1.0.0+  
[SQLite JDBC](https://github.com/xerial/sqlite-jdbc) 3.21.0.1+  

### Required Modules

[java.sql](https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/module-summary.html)  
* [java.xml](https://docs.oracle.com/en/java/javase/11/docs/api/java.xml/module-summary.html)  
* [java.transaction.xa](https://docs.oracle.com/en/java/javase/11/docs/api/java.transaction.xa/module-summary.html)  
* [java.logging](https://docs.oracle.com/en/java/javase/11/docs/api/java.logging/module-summary.html)  
* [java.base](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/module-summary.html)  

### Source

The `master` branch contains stable code. Until a release is cut, the `master` branch will be shifting. 

### Introduction

A command-line based (at the moment) Doom engine game launcher.

### Why?

Because I'm never satisfied with any Doom launcher programs, so I made this.

### Compiling with Ant

To download dependencies for this project, type (`build.properties` will also be altered/created):

	ant dependencies

To compile this program with Apache Ant, type:

	ant compile

To make Maven-compatible JARs of this program (placed in the *build/jar* directory), type:

	ant jar

To make Javadocs (placed in the *build/docs* directory):

	ant javadoc

To compile main and test code and run tests (if any):

	ant test

To make Zip archives of everything (main src/resources, bin, javadocs, placed in the *build/zip* directory):

	ant zip

To compile, JAR, test, and Zip up everything:

	ant release

To create a distribution (Bash and CMD):

	ant dist

To create a distribution and deploy it (THIS WILL DELETE AND REBUILD THE TARGET DIRECTORY):

	ant deploy.cmd -Ddeploy.dir=[TARGETPATH]
	ant deploy.bash -Ddeploy.dir=[TARGETPATH]

To clean up everything:

	ant clean

### Other

This program/library and the accompanying materials
are made available under the terms of the GNU Lesser Public License v2.1
which accompanies this distribution, and is available at
http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html

A copy of the LGPL should have been included in this release (LICENSE.txt).
If it was not, please contact me for a copy, or to notify me of a distribution
that has not included it. 

This contains code copied from Black Rook Base, under the terms of the MIT License (docs/LICENSE-BlackRookBase.txt).
