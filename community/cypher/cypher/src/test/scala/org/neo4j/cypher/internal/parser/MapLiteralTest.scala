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
package org.neo4j.cypher.internal.parser

import org.neo4j.cypher.internal.planner.v3_6.spi.TokenContext
import org.neo4j.cypher.internal.runtime.interpreted.commands.convert.{CommunityExpressionConverter, ExpressionConverters}
import org.neo4j.cypher.internal.runtime.interpreted.commands.{expressions => legacy}
import org.neo4j.cypher.internal.v3_6.parser.{Expressions, ParserTest}
import org.neo4j.cypher.internal.v3_6.util.attribution.Id
import org.neo4j.cypher.internal.v3_6.{expressions => ast}
import org.parboiled.scala._

class MapLiteralTest extends ParserTest[ast.Expression, legacy.Expression] with Expressions {
  implicit val parserToTest = MapLiteral ~ EOI

  test("literal_maps") {
    parsing("{ name: 'Andres' }") shouldGive
      legacy.LiteralMap(Map("name" -> legacy.Literal("Andres")))

    parsing("{ meta : { name: 'Andres' } }") shouldGive
      legacy.LiteralMap(Map("meta" -> legacy.LiteralMap(Map("name" -> legacy.Literal("Andres")))))

    parsing("{ }") shouldGive
      legacy.LiteralMap(Map())
  }

  test("nested_map_support") {
    parsing("{ key: 'value' }") shouldGive
      legacy.LiteralMap(Map("key" -> legacy.Literal("value")))

    parsing("{ inner1: { inner2: 'Value' } }") shouldGive
      legacy.LiteralMap(Map("inner1" -> legacy.LiteralMap(Map("inner2" -> legacy.Literal("Value")))))
  }

  private val converters = new ExpressionConverters(CommunityExpressionConverter(TokenContext.EMPTY))
  def convert(astNode: ast.Expression): legacy.Expression = converters.toCommandExpression(Id.INVALID_ID, astNode)
}
