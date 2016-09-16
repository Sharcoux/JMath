# JMath
## A Swing Component to display MathML

This library provides a component that can display MathML. It is meant to be better than JEuclid in supporting mover and munder tags, reading html, and automatically aligning the component with the text (espacially useful to write fractions).

## Getting started

To create a new component, simply do:

    JMathDisplayer component = new JMathDisplayer(testString);

## Current support

All tags are currently supported, but very few attributes are supported. Other attributes will be supported if needed. Feel free to ask, or participate.

Attributes currently supported:

- mathcolor
- color
- columnspacing
- rowspacing

## Maven

The steps to getting in the Maven Central repository are quite troublesome, so, for now, to include JMath in your project, add this in your pom, in the <repositories> section:

        <repositories>
            ...
            <repository>
                <id>JMath-mvn-repo</id>
                <url>https://raw.github.com/Sharcoux/JMath/mvn-repo/</url>
                <snapshots>
                    <enabled>true</enabled>
                    <updatePolicy>always</updatePolicy>
                </snapshots>
            </repository>
            ...
        </repositories>

You can now import the artefact as usual:
	    <dependencies>
	        ...
		    <dependency>
			    <groupId>com.fbillioud</groupId>
			    <artifactId>jmath</artifactId>
			    <version>1.0.3</version>
		    </dependency>
		    ...
        </dependencies>
        
## Copyright

Copyright 2016 François Billioud.
Licensed under the Apache License, Version 2.0
http://www.apache.org/licenses/LICENSE-2.0
