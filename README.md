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

- 2025: [Translating BPMN models into X-Klaim programs for developing multi-robot missions](https://link.springer.com/article/10.1007/s10009-025-00832-y). Bourr K., Tiezzi F., Bettini L., Seriani S. International Journal on Software Tools for Technology Transfer, 27(6), 557-575.
- 2024: [Model-Driven Development of Multi-Robot Systems: From BPMN Models to X-Klaim Code](https://doi.org/10.1007/978-3-031-75107-3_14). Bourr K., Tiezzi F., Bettini L. In: Leveraging Applications of Formal Methods, Verification and Validation. Rigorous Engineering of Collective Adaptive Systems. Lecture Notes in Computer Science, vol. 15220. Springer.
- 2023: [Coordinating and programming multiple ROS-based robots with X-KLAIM](https://link.springer.com/article/10.1007/s10009-023-00727-w). Bettini L., Bourr K., Pugliese R., Tiezzi F. International Journal on Software Tools for Technology Transfer, 25(5-6), 747-764.
- 2022: [Programming Multi-robot Systems with X-Klaim](https://doi.org/10.1007/978-3-031-19759-8_18). Bettini L., Bourr K., Pugliese R., Tiezzi F. In: Leveraging Applications of Formal Methods, Verification and Validation. Adaptation and Learning. Lecture Notes in Computer Science, vol. 13703. Springer.
- 2020: [Writing Robotics Applications with X-Klaim](https://doi.org/10.1007/978-3-030-61470-6_22). Bettini L., Bourr K., Pugliese R., Tiezzi F. In: Leveraging Applications of Formal Methods, Verification and Validation: Engineering Principles. Lecture Notes in Computer Science, vol. 12477. Springer.
- 2019: [X-Klaim Is Back](https://doi.org/10.1007/978-3-030-21485-2_8). Bettini L., Merelli E., Tiezzi F. In: Boreale M., Corradini F., Loreti M., Pugliese R. (eds) Models, Languages, and Tools for Concurrent and Distributed Programming. Lecture Notes in Computer Science, vol. 11665. Springer.
- 2007: [Implementing a Distributed Mobile Calculus Using the IMC Framework](https://doi.org/10.1016/j.entcs.2007.01.054). Bettini L., De Nicola R., Falassi D., Loreti M. Electronic Notes in Theoretical Computer Science, 181, 63-79.
- 2006: [Implementing Mobile and Distributed Applications in X-Klaim](https://scpe.org/index.php/scpe/article/view/384). Bettini L., De Nicola R., Loreti M. Scalable Computing: Practice and Experience, 7(4), 13-35.
- 2005: [Mobile Distributed Programming in X-Klaim](https://doi.org/10.1007/11419822_2). Bettini L., De Nicola R. In: Bernardo M., Bogliolo A. (eds) Formal Methods for Mobile Computing, 5th International School on Formal Methods for the Design of Computer, Communication, and Software Systems, SFM-Moby 2005, Bertinoro, Italy, April 26-30, 2005, Advanced Lectures. Lecture Notes in Computer Science, vol. 3465. Springer.
- 2002: [Software Update via Mobile Agent Based Programming](https://doi.org/10.1145/508791.508800). Bettini L., De Nicola R., Loreti M. In: Proceedings of the 2002 ACM Symposium on Applied Computing (SAC), March 10-14, 2002, Madrid, Spain.
- 2002: [X-Klaim and Klava: Programming Mobile Code](https://doi.org/10.1016/S1571-0661(04)00317-2). Bettini L., De Nicola R., Pugliese R. Electronic Notes in Theoretical Computer Science, 62, 24-37.
- 2002: [Klava: a Java package for distributed and mobile applications](https://doi.org/10.1002/spe.486). Bettini L., De Nicola R., Pugliese R. Software: Practice and Experience, 32(14), 1365-1394.
- 2001: [Modelling Node Connectivity in Dynamically Evolving Networks](https://doi.org/10.1016/S1571-0661(04)00237-3). Bettini L., Loreti M., Pugliese R. Electronic Notes in Theoretical Computer Science, 54, 81-91.
- 2000: [Mobile Applications in X-KLAIM](https://dblp.org/rec/conf/woa/BettiniNFP00). Bettini L., De Nicola R., Ferrari G.-L., Pugliese R. WOA 2000, 1-6.
- 2000: [Structured Nets in Klaim](https://hdl.handle.net/2158/240726). Bettini L., Loreti M., Pugliese R. Proceedings of the 2000 ACM Symposium on Applied Computing (SAC'00), Special Track on Coordination Models, Languages and Applications, 174-180.
- 1998: [Interactive Mobile Agents in X-Klaim](https://doi.org/10.1109/ENABL.1998.725680). Bettini L., De Nicola R., Pugliese R., Ferrari G. In: Proceedings of the 7th IEEE International Workshops on Enabling Technologies: Infrastructure for Collaborative Enterprises (WETICE), 110-115.
