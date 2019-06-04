[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.foellix/AQL-System/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.foellix/AQL-System) ![Java 8](https://img.shields.io/badge/java-8-brightgreen.svg)
---
<p align="center">
	<img src="https://FoelliX.github.io/AQL-System/logo.png" width="300px"/>
</p>

# AQL
The *Android App Analysis Query Language (AQL)* consists of two main parts, namely *AQL-Queries* and *AQL-Answers*.
AQL-Queries enable us to ask for Android specific analysis subjects in a general, tool independent way.
The grammar defining AQL-Queries can be found [here](https://github.com/FoelliX/AQL-System/wiki "Grammar write up").

### Example 1: AQL-Queries
The following exemplary query can be used to get all ``Flows`` (e.g. taint flows) inside one app:
```
Flows IN App(’/path/to/example.apk’) ?
```

it is also possible to ask more specifically (or to filter the result):
```
Flows FROM 
	Statement(’getDeviceId()’)
	->Method(’onCreate(...)’)
	->Class(’MainActivity’)
	->App(’/path/to/example1.apk’) 
TO 
	Statement(’sendTextMessage(...)’)
	->Method(’onCreate(...)’)
	->Class(’MainActivity’)
	->App(’/path/to/example2.apk’)
?
```

Different operators are available to merge and further filter queries as well as methods to match intents and intent-filters.  
More information about AQL-Queries can be found [here](https://github.com/FoelliX/AQL-System/wiki).

Similarly, AQL-Answers are used to represent analysis results in a standardized form.  
The syntax of AQL-Answers is defined via an [XML schema definition (XSD)](https://github.com/FoelliX/AQL-System/blob/master/schemas/answer.xsd).

### Example 2: AQL-Answers
An AQL-Answer to the query from above could be (shortened):
```xml
<answer>
	<flows>
		<flow>
			<reference type="from">
				<statement>... getDeviceId() ...</statement>
				<method>... onCreate(...) ...</method>
				<classname>... MainActivity</classname>
				<app>
					<file>.../DirectLeak1.apk</file>
					<hashes>...</hashes>
				</app>
			</reference>
			<reference type="to">
				...
				sendTextMessage(...)
				...
			</reference>
		</flow>
	</flows>
</answer>
```
It shows a taint flow from a ``getDeviceId()`` statement to a ``sendTextMessage(...)`` statement.

# AQL-System
The associated AQL-System takes AQL-Queries as input and outputs AQL-Answers.  
To do so, it requires a configuration in form of an .xml file that describes
- which tools are avaliable in a certain instance of the AQL-System and how to execute these,
- which queries can be answered by which tool and
- how to convert a tool’s result into an AQLAnswer.

### Example 3
For instance, an AQL-System can be configured to execute [FlowDroid](https://github.com/secure-software-engineering/FlowDroid) in case of intra-app flow questions and [IccTA](https://github.com/lilicoding/soot-infoflow-android-iccta) in case of inter-app questions, since FlowDroid does not support such questions.
Considering the example from above the AQL-System recognizes that FlowDroid is available and able to answer the query regarding flows inside one app only.
Consequently, FlowDroid is launched and its result is converted into an AQL-Answer.



## Usage
Our wiki contains tutorials how to use/extend the AQL-System: [here](https://github.com/FoelliX/AQL-System/wiki)

### Execution
One [tutorial](https://github.com/FoelliX/AQL-System/wiki) deals with a simple run-through.
It guides the user how to install, configure and execute an AQL-System, which is setup to use [Amandroid](http://pag.arguslab.org/argus-saf).

### Development
**Library**  
The AQL-System or only its internal datastructure (AQL-Lib) can be used by or in other tools. We offer to options for integration:
- Maven

```xml
<dependency>
	<groupId>de.foellix</groupId>
	<artifactId>AQL-System</artifactId>
	<version>1.2.0</version>
</dependency>
```

- .jar Import  
All releases can be found [here](https://github.com/FoelliX/AQL-System/releases/).

**Building from source code**  
- Import *Maven project* to Eclipse
- Build pom.xml as *Maven project*
	- Build is stored inside the project's directory: ``projectDirectory/target/build``  
	*(``projectDirectory`` refers to your local project directory and consequently has to be replaced by the actual directory and its path)*
- Run the build ([Launch parameters](https://github.com/FoelliX/AQL-System/wiki)):
	- Option 1:
	```bash
	cd projectDirectory/target/build
	java -jar AQL-System-XXX.jar
	```
	- Option 2: Run ``de.foellix.aql.ui.cli.CommandLineInterface`` as *Java Application*


## Publications
- *Do Android Taint Analysis Tools Keep Their Promises?* (Felix Pauck, Eric Bodden, Heike Wehrheim)  
ESEC/FSE 2018 [https://dl.acm.org/citation.cfm?id=3236029](https://dl.acm.org/citation.cfm?id=3236029)

## License
The AQL-System is licensed under the *GNU General Public License v3* (see [LICENSE](https://github.com/FoelliX/AQL-System/blob/master/LICENSE)).

# Contact
**Felix Pauck** (FoelliX)  
Paderborn University  
fpauck@mail.uni-paderborn.de  
[http://www.FelixPauck.de](http://www.FelixPauck.de)

# Links
- The AQL-System is part of BREW: [https://github.com/FoelliX/BREW](https://github.com/FoelliX/BREW)
- which is used in the ReproDroid toolchain: [https://github.com/FoelliX/ReproDroid](https://github.com/FoelliX/ReproDroid)