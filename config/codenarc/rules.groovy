ruleset {
  description 'Rules for the BioGroovy project'

  ruleset('rulesets/basic.xml') 
  ruleset('rulesets/groovyism.xml')
  ruleset('rulesets/braces.xml')
  ruleset('rulesets/exceptions.xml')
  ruleset('rulesets/imports.xml')
  ruleset('rulesets/logging.xml') {
    'Println' priority: 1
    'PrintStackTrace' priority: 1
  }
  ruleset('rulesets/naming.xml') {
    'MethodName' enabled: false // Spock uses method names that make codenarc barf
  }
  ruleset('rulesets/unnecessary.xml') {
    'UnnecessaryPackageReference' enabled: false
  }
  ruleset('rulesets/unused.xml')
}
