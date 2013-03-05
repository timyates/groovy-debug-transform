# groovy-debug-transform

## Work in progress...

Messing about with the groovy AST.

Annotate a method with `@Debug` and it should `println` debug info before each line is executed

Currently, given a class:

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

      static main( args ) {
        println new Test().run()
      }
    }

When it is executed, it prints out:

    Tue Mar 05 14:12:37 GMT 2013 (line 006) EXPRESSION: (a = 2)
    Tue Mar 05 14:12:37 GMT 2013 (line 007) EXPRESSION: (b = tim)
    Tue Mar 05 14:12:37 GMT 2013 (line 008) IF        : ((a == 3) || (b <=> tim)) where a = 2, b = tim
    Tue Mar 05 14:12:37 GMT 2013 (line 013) RETURN    : 2
    2

Obviously, this can be improved, but this is a work in progress...

### To run yourself:

Build the jar

    ./gradlew jar

Then, run the test script

    groovy -cp .:build/libs/groovy-debug-transform-0.1.jar Test.groovy

# Todo

- Handle more constructs
- Allow output other than System.out
- More tests
