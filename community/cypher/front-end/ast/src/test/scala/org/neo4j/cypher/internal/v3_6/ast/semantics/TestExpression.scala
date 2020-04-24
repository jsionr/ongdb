/*
 * Copyright (c) 2018-2020 "Graph Foundation"
 * Graph Foundation, Inc. [https://graphfoundation.org]
 *
 * Copyright (c) 2002-2020 "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.cypher.internal.v3_6.ast.semantics

import org.neo4j.cypher.internal.v3_6.expressions.Expression
import org.neo4j.cypher.internal.v3_6.expressions.Expression.SemanticContext
import org.neo4j.cypher.internal.v3_6.util.symbols.TypeSpec
import org.neo4j.cypher.internal.v3_6.util.{DummyPosition, InputPosition}

case class ErrorExpression(
                            error: SemanticError,
                            possibleTypes: TypeSpec,
                            position: InputPosition = DummyPosition(0)
                          ) extends Expression

case class CustomExpression(
                             semanticCheck: (SemanticContext, CustomExpression) => SemanticCheck,
                             position: InputPosition = DummyPosition(0)
                           ) extends Expression
