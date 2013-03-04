package com.bloidonia.groovy.ast.debug

import spock.lang.Specification

class GeneralTests extends Specification {
  def 'test assignment'() {
    given:
      def c = new GroovyClassLoader().parseClass('''class MyClass {
                                                      @com.bloidonia.groovy.ast.debug.Debug
                                                      static ret() {
                                                        4
                                                      }            
                                                    }''')  
    expect:
      c.ret() == 4
  }
}