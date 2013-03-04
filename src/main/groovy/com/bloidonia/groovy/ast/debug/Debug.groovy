package com.bloidonia.groovy.ast.debug

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import org.codehaus.groovy.transform.GroovyASTTransformationClass

@Retention( RetentionPolicy.SOURCE )
@Target( [ ElementType.METHOD ] )
@GroovyASTTransformationClass( 'com.bloidonia.groovy.ast.debug.DebugTransformation' )
@interface Debug {
}