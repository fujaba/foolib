# foolib - Fujaba Object Oriented library

[![Java CI](https://github.com/fujaba/foolib/workflows/Java%20CI/badge.svg)](https://github.com/fujaba/foolib/actions)

Foolib is a library that provides code generation for UML like models and some model management functionalities.
Using a domain-specific language provided by Java APIs, it allows you to define classes, attributes and associations with a meta model.
From the meta model definition, Foolib can automatically generate Java code.
The generated code ensures referential integrity and can optionally include support for property change listeners or JavaFX.

Foolib-generated Java files can seamlessly coexist with hand-written code and may even be modified.
Great care is taken that no hand-written code is deleted or changed by code generation.
Foolib is able to read and modify any Java code using language features from up to Java 11,
even if it contains syntax errors or can otherwise not be compiled.

We have an Online Version at www.foolib.org where you can find docs and tutorials for getting started.

## Installation

### Gradle

`build.gradle`:

```groovy
plugins {
    // ...
    // https://plugins.gradle.org/plugin/org.foolib.foolibGradle
    id 'org.foolib.foolibGradle' version '0.5.0'
}

repositories {
    // ...
    mavenCentral()
}

dependencies {
    // ...

    // https://mvnrepository.com/artifact/org.foolib/foolibScenarios
    foolibScenarios group: 'org.foolib', name: 'foolibScenarios', version: '1.6.2'

    // optional, to override the version of foolib used by foolibScenarios:
    // https://mvnrepository.com/artifact/org.foolib/foolib
    foolibScenarios group: 'org.foolib', name: 'foolib', version: '1.5.1'
}
```

### Maven

Maven is currently not supported.
See [issue #52](https://github.com/fujaba/foolib/issues/52) for more info.

## Usage

Check out the [Quickstart Guide](docs/quickstart/README.md) or the [detailed documentation](docs/README.md) to learn how to use foolib.

## History

Foolib is the newest tool of the Fujaba Family https://github.com/fujaba .

| Period | Activity |
| --- | --- |
| 1998 - 2008 | We developed the Fujaba (From UML to Java And Back Again) tool as a graphical editor for class diagrams and model transformations. |
| 2008 - 2018 | We moved on to [SDMLib](https://github.com/fujaba/SDMLib) (Story Driven Modeling Library). SDMLib got rid of the graphical editors. |
| 2019 - 2021 | We did a major refactoring of the SDMLib and call it Fulib (Fujaba Library) now. |
| April 1, 2021 - present | We decided to drop fulib and move on to the newest installment, **foolib**. |

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

[MIT](LICENSE.md)
