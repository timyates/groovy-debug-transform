import com.bloidonia.groovy.ast.debug.Debug

class Test {
  @Debug
  def run() {
    int a = 2
    def b = 'tim'
    if( a == 3 || b <=> 'tim' ) {
      println "Woo $a"
      4
    }
    else {
      2
    }
  }

  def run2() {
    int a = 2
    def b = 'tim'
    if( a == 3 || b <=> 'tim' ) {
      println "Woo $a"
      4
    }
    else {
      2
    }
  }
  
  static main( args ) {
    println new Test().run()
  }
}