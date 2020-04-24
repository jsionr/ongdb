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
package org.neo4j.cypher


import System.lineSeparator

import org.neo4j.cypher.internal.v3_6.util.test_helpers.CypherFunSuite

class SyntaxExceptionTest extends CypherFunSuite {

  test("caret renders correctly for single line query") {
    val ex = new SyntaxException("msg", "query", 3)
    val msg = ex.getMessage
    msg should equalString(
      """msg
        |"query"
        |    ^""")
  }

  test("caret renders correctly under X for multiline query using \\n") {
    val ex = new SyntaxException("msg", "line 1\nline 2 X marks the spot", 14)
    val msg = ex.getMessage
    msg should equalString(
      """msg
        |"line 2 X marks the spot"
        |        ^""")
  }

  test("caret renders correctly under X for multiline query using \\r\\n") {
    val ex = new SyntaxException("msg", "line 1\r\nline 2 X marks the spot", 15)
    val msg = ex.getMessage
    msg should equalString(
      """msg
        |"line 2 X marks the spot"
        |        ^""")
  }

  test("caret renders at the end when offset is too large") {
    val ex = new SyntaxException("msg", "line 1\r\nline 2", 1000)
    val msg = ex.getMessage
    msg should equalString(
      """msg
        |"line 2"
        |       ^""")
  }

  test("caret renders at the end when query is empty") {
    val ex = new SyntaxException("msg", "", 0)
    val msg = ex.getMessage
    msg should equalString(
      """msg
        |""
        | ^""")
  }

  test("no caret renders when no offset is given") {
    val ex = new SyntaxException("msg", "query", None)
    val msg = ex.getMessage
    msg should equalString("msg")
  }

  private def equalString(s: String) =
    equal(s
      .stripMargin
      .lines
      .mkString(lineSeparator())) // make sure test data does not depend on git and/or IDE settings and always uses system line separator

}
