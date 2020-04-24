/*
 * Copyright (c) 2018-2020 "Graph Foundation"
 * Graph Foundation, Inc. [https://graphfoundation.org]
 *
 * Copyright (c) 2002-2020 "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of ONgDB.
 *
 * ONgDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.runtime.interpreted.commands.expressions

import org.neo4j.cypher.internal.runtime.interpreted.commands.AstNode
import org.neo4j.cypher.internal.runtime.interpreted.pipes.QueryState
import org.neo4j.cypher.internal.runtime.interpreted.{ExecutionContext, GraphElementPropertyFunctions}
import org.neo4j.values.AnyValue
import org.neo4j.values.virtual.MapValueBuilder

import scala.collection.Map

case class LiteralMap(data: Map[String, Expression]) extends Expression with GraphElementPropertyFunctions {

  override def apply(ctx: ExecutionContext, state: QueryState): AnyValue = {
    val builder = new MapValueBuilder
    data.foreach {
      case (k, e) => builder.add(k, e(ctx, state))
    }
    builder.build()
  }

  override def rewrite(f: Expression => Expression): Expression = f(LiteralMap(data.rewrite(f)))

  override def arguments: Seq[Expression] = data.values.toIndexedSeq

  override def children: Seq[AstNode[_]] = arguments

  override def symbolTableDependencies: Set[String] = data.symboltableDependencies

  override def toString: String = "LiteralMap(" + data + ")"
}
