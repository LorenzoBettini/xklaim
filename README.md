# xklaim

[![Java CI with Maven](https://github.com/LorenzoBettini/xklaim/actions/workflows/linux.yml/badge.svg)](https://github.com/LorenzoBettini/xklaim/actions/workflows/linux.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.lorenzobettini.klaim/xklaim.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.lorenzobettini.klaim/xklaim)

Java 21 is required.

Eclipse update site: https://lorenzobettini.github.io/xklaim-releases/

Eclipse distributions with Xklaim installed: https://sourceforge.net/projects/xklaim/files/products/

## Documentation

- [X-KLAIM user guide](documentation/XKLAIM_USER_GUIDE.md): language syntax, tuple-space operations, process mobility, IDE workflow, and examples.
- [KLAVA user guide](documentation/KLAVA_USER_GUIDE.md): Java runtime concepts behind tuple spaces, localities, matching, blocking operations, and mobility.
- [IMC user guide](documentation/IMC_USER_GUIDE.md): lower-level communication and networking layer used by KLAVA and X-KLAIM.
- [Code generation notes](documentation/CODEGEN_NOTES.md): generated Java structure, lifecycle, and compiler-oriented details.
- [Logging notes](documentation/LOGGING_NOTES.md): SLF4J setup and logging configuration guidance.
- [KLAVA internal notes](documentation/KLAVA_NOTES.md) and [IMC internal notes](documentation/IMC_NOTES.md): implementation-level reference notes.

**For macOS users**: depending on the version of your macOS, when you try to run the `xklaim.app` you may run into an error that says "the application is damaged and can't be opened". This problem can be overcome by running the following command from the terminal (from the directory where the `xklaim.app` is located): `xattr -c xklaim.app`.

**IMPORTANT**: _the old Bintray update site does not work anymore, make sure you use the new one and remove the old one_. If you had downloaded an Xklaim Eclipse distribution earlier than 2.1 you won't be able to update it; please download a brand new Xklaim Eclipse distribution.

**Publications:**

https://link.springer.com/chapter/10.1007%2F978-3-030-21485-2_8

Bettini L., Merelli E., Tiezzi F. (2019) X-Klaim Is Back. In: Boreale M., Corradini F., Loreti M., Pugliese R. (eds) Models, Languages, and Tools for Concurrent and Distributed Programming. Lecture Notes in Computer Science, vol 11665. Springer.

https://link.springer.com/chapter/10.1007/978-3-030-61470-6_22

Bettini L., Bourr K., Pugliese R., Tiezzi F. (2020) Writing Robotics Applications with X-Klaim. In: Leveraging Applications of Formal Methods, Verification and Validation: Engineering Principles. Lecture Notes in Computer Science, vol 12477. Springer.
