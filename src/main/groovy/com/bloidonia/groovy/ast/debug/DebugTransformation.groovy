/**
 * Copyright (C)2013 - Tim Yates <tim@bloidonia.com>
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
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bloidonia.groovy.ast.debug

import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.MethodNode

import org.codehaus.groovy.ast.builder.AstBuilder

import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.VariableExpression

import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement

import org.codehaus.groovy.classgen.ReturnAdder

import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit

import org.codehaus.groovy.control.messages.Message
import org.codehaus.groovy.control.messages.SyntaxErrorMessage

import org.codehaus.groovy.syntax.SyntaxException

import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation( phase = CompilePhase.SEMANTIC_ANALYSIS )
class DebugTransformation implements ASTTransformation {

  def annotationType = Debug.name

  private boolean checkNode( astNodes, annotationType ) {
    if( !  astNodes    ) { return false }
    if( !  astNodes[0] ) { return false }
    if( !  astNodes[1] ) { return false }
    if( !( astNodes[0] instanceof AnnotationNode ) ) { return false }
    if( !  astNodes[0].classNode?.name == annotationType ) { return false }
    if( !( astNodes[1] instanceof MethodNode ) ) { return false }
    true
  }
   
  void visit( ASTNode[] astNodes, SourceUnit sourceUnit ) {
    if( !checkNode( astNodes, annotationType ) ) {
      //From LogASTTransformation
      addError( 'Internal error: expecting [AnnotationNode, MethodNode] but got: ' + astNodes.toList(), astNodes[0], sourceUnit )
      return
    }

    MethodNode method = astNodes[ 1 ]

    new ReturnAdder().visitMethod( method )
    addDebug( method )
    addEntryDebug( method )
  }

  private String pad( int num ) {
    "$num".padLeft( 3, '0')
  }

  private List<String> collectExpressionVars( Expression e ) {
    switch( e ) {
      case BinaryExpression:
        return [e.leftExpression,e.rightExpression].collect { collectExpressionVars( it ) }.flatten().unique()
      case BooleanExpression:
        return collectExpressionVars( e.expression )
      case ConstantExpression:
        return []
      case VariableExpression:
        return [ e.name ]
      default:
        return [ e.getClass().name ]
    }
  }

  private String decorate( Statement stmt ) {
    switch( stmt ) {
      case ExpressionStatement:
        return "println '${new Date()} (line ${pad( stmt.lineNumber )}) EXPRESSION: $stmt.expression.text'"
      case IfStatement:
        return "println \"${new Date()} (line ${pad( stmt.lineNumber )}) IF        : $stmt.booleanExpression.text where ${collectExpressionVars( stmt.booleanExpression ).collect { it + ' = $' + it }.join(', ')}\""
      case ReturnStatement:
        return "println '${new Date()} (line ${pad( stmt.lineNumber )}) RETURN    : $stmt.expression.text'"
      default:
        return "println '${new Date()} ${pad( stmt.lineNumber )} ${stmt.text}'"
    }
    
  }

  private List<ASTNode> generatePrintln( String msg ) {
    [ new AstBuilder().buildFromString( "$msg ; println() " )[ 0 ].statements[ 0 ] ]
  }

  private Statement debugStatement( statement ) {
    if( statement instanceof BlockStatement ) {
      new BlockStatement().with { bs ->
        statement.statements.each {
          bs.addStatements( generatePrintln( decorate( it ) ) )
          if( it instanceof IfStatement ) {
            it.ifBlock = debugStatement( it.ifBlock )
            if( it.elseBlock ) {
              it.elseBlock = debugStatement( it.elseBlock )
            }
          }
          bs.addStatement( it )
        }
        bs
      }
    }
    else {
      new BlockStatement().with { bs ->
        bs.addStatements( generatePrintln( decorate( "${statement.getClass().name}" ) ) )
        bs.addStatement( statement )
        bs
      }
    }
  }

  private void addDebug( MethodNode method ) {
    method.code = debugStatement( method.code )
  }

  private void addEntryDebug( MethodNode method ) {
    new BlockStatement().with { bs ->
      addStatements( generatePrintln( "${new Date()} ENTRY: $method.name( $method.parameters )" ) )
      if( method.code instanceof BlockStatement ) {
        BlockStatement methodBlock = (BlockStatement)method.code
        methodBlock.statements.each { statement ->
          if( method instanceof ConstructorNode &&
              statement instanceof ExpressionStatement &&
              statement.expression instanceof ConstructorCallExpression ) {
            bs.statements.add( 0, statement )
          } else {
            bs.statements.add( statement )
          }
        }
      } else {
        bs.addStatement( method.code )
      }
      method.code = bs
    }
  }

  void addError(String msg, ASTNode node, SourceUnit source) {
    SyntaxException se = new SyntaxException( msg + '\n', node.lineNumber, node.columnNumber )
    Message message = new SyntaxErrorMessage( se, source )
    source.errorCollector.addErrorAndContinue( message )
  }
}