/*
 * #%L
 * Native ARchive plugin for Maven
 * %%
 * Copyright (C) 2002 - 2014 NAR Maven Plugin developers.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.maven_nar.cpptasks.borland;

import com.github.maven_nar.cpptasks.compiler.AbstractProcessor;
import com.github.maven_nar.cpptasks.compiler.TestAbstractCompiler;

/**
 * Borland C++ Compiler adapter tests
 *
 * Override create to test concrete compiler implementions
 */
public class TestBorlandCCompiler extends TestAbstractCompiler {
  public TestBorlandCCompiler(final String name) {
    super(name);
  }

  @Override
  protected AbstractProcessor create() {
    return BorlandCCompiler.getInstance();
  }

  @Override
  protected String getObjectExtension() {
    return ".obj";
  }

  @Override
  public void testGetIdentfier() {
  }
}