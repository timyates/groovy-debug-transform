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

## Todo

- Handle more constructs
- Allow output other than System.out
- More tests
