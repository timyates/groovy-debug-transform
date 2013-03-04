package com.bloidonia.groovy.ast.debug

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.messages.Message
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.syntax.SyntaxException
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.classgen.ReturnAdder
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement

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

  private String decorate( Statement stmt ) {
    switch( stmt ) {
      case ExpressionStatement:
        return "println '${new Date()} (line ${pad( stmt.lineNumber )}) EXPRESSION: $stmt.expression.text'"
      case IfStatement:
        return "println '${new Date()} (line ${pad( stmt.lineNumber )}) IF        : $stmt.booleanExpression.text'"
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