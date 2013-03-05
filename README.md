## groovy-debug-transform

Mostly, me messing about with the groovy AST.

Annotate a method with `@Debug` and it should `println` debug info before each line is executed

Currently, given a method:

    @Debug
    def run() {
      int a = 2
      if( a == 3 ) {
        println "Woo $a"
        4
      }
      else {
        2
      }
    }
    
When it is executed, it prints out:

    Mon Mar 04 15:03:46 GMT 2013 (line 006) EXPRESSION: (a = 2)
    Mon Mar 04 15:03:46 GMT 2013 (line 007) IF        : (a == 3)
    Mon Mar 04 15:03:46 GMT 2013 (line 012) RETURN    : 2

Obviously, this can be improved, but this is a work in progress...

### To run yourself:

    ./gradlew jar
    $ groovy -cp .:build/libs/groovy-debug-transform-0.1.jar Test.groovy
    Tue Mar 05 14:12:37 GMT 2013 (line 006) EXPRESSION: (a = 2)
    Tue Mar 05 14:12:37 GMT 2013 (line 007) EXPRESSION: (b = tim)
    Tue Mar 05 14:12:37 GMT 2013 (line 008) IF        : ((a == 3) || (b <=> tim)) where a = 2, b = tim
    Tue Mar 05 14:12:37 GMT 2013 (line 013) RETURN    : 2
    2

## Todo

- Handle more constructs
- Allow output other than System.out
- More tests
