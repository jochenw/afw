/**
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.rflct;

/** This package contains a set of interfaces, and implementations,
 * which are intended to replace Java reflection with
 * {@link java.lang.invoke.MethodHandle method handles}. By using
 * these interfaces, and implementations, you gain the possibilities,
 * that Java reflection provides, in a manner, which is agnostic of
 * JVM versions.
 * These interfaces, and implementations are:
 * <table>
 *   <tr><th>Interface</th><th>Description</th></tr>
 *   <tr><td>{@link IInstantiator}</td><td>Object creation</td></tr>
 * </table>
 */