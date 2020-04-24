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
package org.neo4j.cypher.internal.v3_6.parser

import org.neo4j.cypher.internal.v3_6.ast
import org.neo4j.cypher.internal.v3_6.ast.Where
import org.neo4j.cypher.internal.v3_6.{expressions => exp}
import org.neo4j.cypher.internal.v3_6.util.InputPosition
import org.parboiled.scala._

trait ProcedureCalls {
  self: Parser with Base with Expressions with Literals =>

  def Call: Rule1[ast.UnresolvedCall] = rule("CALL") {
    group(keyword("CALL") ~~ Namespace ~ ProcedureName ~ ProcedureArguments ~~ ProcedureResult) ~~>> (ast.UnresolvedCall(_, _, _, _))
  }

  private def ProcedureArguments: Rule1[Option[Seq[org.neo4j.cypher.internal.v3_6.expressions.Expression]]] = rule("arguments to a procedure") {
    optional(group("(" ~~
      zeroOrMore(Expression, separator = CommaSep) ~~ ")"
    ) ~~> (_.toIndexedSeq))
  }

  private def ProcedureResult =
    rule("result fields of a procedure") {
      optional(
        group(
          keyword("YIELD") ~~
          oneOrMore(ProcedureResultItem, separator = CommaSep) ~~
          optional(group(keyword("WHERE") ~~ Expression ~~>> (Where(_))))
        ) ~~> { (a, b) => a -> b } ~~>> (procedureResult(_))
      )
    }

  private def procedureResult(data: (List[ast.ProcedureResultItem], Option[Where]))(pos: InputPosition) = {
    val (items, optWhere) = data
    ast.ProcedureResult(items.toIndexedSeq, optWhere)(pos)
  }

  private def ProcedureResultItem: Rule1[ast.ProcedureResultItem] =
    AliasedProcedureResultItem | SimpleProcedureResultItem

  private def AliasedProcedureResultItem: Rule1[ast.ProcedureResultItem] =
    rule("aliased procedure result field") {
      ProcedureOutput ~~ keyword("AS") ~~ Variable ~~>> (ast.ProcedureResultItem(_, _))
    }

  private def SimpleProcedureResultItem: Rule1[ast.ProcedureResultItem] =
    rule("simple procedure result field") {
      Variable ~~>> (ast.ProcedureResultItem(_))
    }

  private def ProcedureOutput: Rule1[org.neo4j.cypher.internal.v3_6.expressions.ProcedureOutput] =
    rule("procedure output") {
      SymbolicNameString ~~>> (exp.ProcedureOutput(_))
    }
}
