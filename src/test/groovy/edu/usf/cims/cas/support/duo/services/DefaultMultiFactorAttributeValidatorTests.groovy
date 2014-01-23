package edu.usf.cims.cas.support.duo.services

import spock.lang.Specification

class DefaultMultiFactorAttributeValidatorTests extends Specification {

  def "No keys in common"(){
      given:
        def serviceAttributes = [attributeOne: "foo"]
        def userAttributes = [attributeTwo: "foo"]


      when:
        def validator = new DefaultMultiFactorAttributeValidator()
        def result = validator.check(serviceAttributes,userAttributes)

      then:
        result == false
  }

  def "One key and value in common"(){
      given:
        def serviceAttributes = [attributeOne: "foo"]
        def userAttributes = [attributeOne: "foo"]


      when:
        def validator = new DefaultMultiFactorAttributeValidator()
        def result = validator.check(serviceAttributes,userAttributes)

      then:
        result == true
  }

  def "Values differ in case"(){
      given:
        def serviceAttributes = [attributeone: "FOO"]
        def userAttributes = [attributeone: "foo"]


      when:
        def validator = new DefaultMultiFactorAttributeValidator()
        def result = validator.check(serviceAttributes,userAttributes)

      then:
        result == true
  }

  def "Keys differ in case"(){
      given:
        def serviceAttributes = [ATTRIBUTEONE: "foo"]
        def userAttributes = [attributeone: "foo"]


      when:
        def validator = new DefaultMultiFactorAttributeValidator()
        def result = validator.check(serviceAttributes,userAttributes)

      then:
        result == true
  }

  def "One key and value in common (mutliple user values)"(){
      given:
        def serviceAttributes = [attributeOne: "foo"]
        def userAttributes = [attributeOne: ["FOO","bar"]]


      when:
        def validator = new DefaultMultiFactorAttributeValidator()
        def result = validator.check(serviceAttributes,userAttributes)

      then:
        result == true
  }

  def "One key and no values in common"(){
      given:
        def serviceAttributes = [attributeOne: "foo"]
        def userAttributes = [attributeOne: "bar"]


      when:
        def validator = new DefaultMultiFactorAttributeValidator()
        def result = validator.check(serviceAttributes,userAttributes)

      then:
        result == false
  }

  def "One key and one value in list in common"(){
      given:
        def serviceAttributes = [attributeOne: ["foo", "bar"] ]
        def userAttributes = [attributeOne: "bar"]


      when:
        def validator = new DefaultMultiFactorAttributeValidator()
        def result = validator.check(serviceAttributes,userAttributes)

      then:
        result == true
  }

  def "One key and multiple values in list in common"(){
      given:
        def serviceAttributes = [attributeOne: ["foo", "bar"] ]
        def userAttributes = [attributeOne: ["bar", "foo"] ]


      when:
        def validator = new DefaultMultiFactorAttributeValidator()
        def result = validator.check(serviceAttributes,userAttributes)

      then:
        result == true
  }

  def "One key and one value in list in common 2"(){
      given:
        def serviceAttributes = [attributeOne: ["foo", "bar"] ]
        def userAttributes = [attributeOne: ["baz", "foo"] ]


      when:
        def validator = new DefaultMultiFactorAttributeValidator()
        def result = validator.check(serviceAttributes,userAttributes)

      then:
        result == true
  }

  def "One key and no values in list in common"(){
      given:
        def serviceAttributes = [attributeOne: ["foo", "bar"] ]
        def userAttributes = [attributeOne: ["baz", "blah"] ]


      when:
        def validator = new DefaultMultiFactorAttributeValidator()
        def result = validator.check(serviceAttributes,userAttributes)

      then:
        result == false
  }

  def "Multiple keys and no values in list in common"(){
      given:
        def serviceAttributes = [attributeOne: ["foo", "bar"],  attributeTwo: ["baz", "blah"]]
        def userAttributes = [attributeOne: ["baz", "blah"] ]


      when:
        def validator = new DefaultMultiFactorAttributeValidator()
        def result = validator.check(serviceAttributes,userAttributes)

      then:
        result == false
  }

  def "Multiple keys and one value in list in common"(){
      given:
        def serviceAttributes = [attributeOne: ["foo", "bar"],  attributeTwo: ["baz", "blah"]]
        def userAttributes = [attributeTwo: ["test", "blah"] ]

      when:
        def validator = new DefaultMultiFactorAttributeValidator()
        def result = validator.check(serviceAttributes,userAttributes)

      then:
        result == true
  }

  def "Multiple keys in both lists and one value in list in common"(){
      given:
        def serviceAttributes = [attributeOne: ["foo", "BAR"],  attributeTwo: ["baz", "blah"]]
        def userAttributes = [attributeTwo: ["test", "bar"], attributeOne: ["test", "bar"] ]

      when:
        def validator = new DefaultMultiFactorAttributeValidator()
        def result = validator.check(serviceAttributes,userAttributes)

      then:
        result == true
  }
}