# afw

The **A**pplication **F**rame**w**ork is a small Java library, that can act as a basis for implementing
a Java application. It includes

1. A simple, but fast implementation of JSR 330 (dependency injection).
2. An API, which allows to use the above dependency injection framework, while maintaining the ability
   to replace it with another DI framework, like Google Guice, or Spring Beans, if necessary.
3. A small logging facade, roughly like [SLF4J](https://www.slf4j.org/), that allows to implement
   logging by targeting arbitrary logging backends, like Log4J2, or others.
4. A large set of utility classes for all aspects of Java programming, including I/O, String
   operations, execution of external programs, and the like.

## License

 Copyright 2018-2022 Jochen Wiedmann

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
