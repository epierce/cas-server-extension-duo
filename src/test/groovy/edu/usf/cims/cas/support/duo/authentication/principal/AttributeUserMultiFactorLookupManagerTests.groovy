package edu.usf.cims.cas.support.duo.authentication.principal

import spock.lang.Specification
import org.jasig.cas.authentication.principal.Principal

class AttributeUserMultiFactorLookupManagerTests extends Specification {

  def principal = Mock(Principal)

  def "User does not have the attribute"(){
      given:
        principal.id >> "testUser"
        principal.attributes >> [attributeTwo: "foo"]

      when:
        def manager = new AttributeUserMultiFactorLookupManager()
        def result = manager.getMFARequired(principal)

      then:
        result == false
  }

  def "User has the attribute, but wrong value"(){
      given:
        principal.id >> "testUser"
        principal.attributes >> [casMFARequired: "NO"]

      when:
        def manager = new AttributeUserMultiFactorLookupManager()
        def result = manager.getMFARequired(principal)

      then:
        result == false
  }

  def "User has the attribute and correct value"(){
      given:
        principal.id >> "testUser"
        principal.attributes >> [casMFARequired: "YES"]

      when:
        def manager = new AttributeUserMultiFactorLookupManager()
        def result = manager.getMFARequired(principal)

      then:
        result == true
  }

  def "User has the attribute and correct value (multiple attributes)"(){
      given:
        principal.id >> "testUser"
        principal.attributes >> [casMFARequired: "YES", attributeTwo: "foo"]

      when:
        def manager = new AttributeUserMultiFactorLookupManager()
        def result = manager.getMFARequired(principal)

      then:
        result == true
  }

  def "User has the attribute and correct value (multiple values)"(){
      given:
        principal.id >> "testUser"
        principal.attributes >> [casMFARequired: ["YES","foo"]]

      when:
        def manager = new AttributeUserMultiFactorLookupManager()
        def result = manager.getMFARequired(principal)

      then:
        result == true
  }

  def "User has the attribute and correct value (custom attribute)"(){
      given:
        principal.id >> "testUser"
        principal.attributes >> [requireDuo: "YES"]

      when:
        def manager = new AttributeUserMultiFactorLookupManager()
        manager.multiFactorAttributeName = "requireDuo"
        def result = manager.getMFARequired(principal)

      then:
        result == true
  }

  def "User has the attribute and correct value (custom attribute and value)"(){
      given:
        principal.id >> "testUser"
        principal.attributes >> [requireDuo: "yup"]

      when:
        def manager = new AttributeUserMultiFactorLookupManager()
        manager.multiFactorAttributeName = "requireDuo"
        manager.multiFactorAttributeValue = "yup"
        def result = manager.getMFARequired(principal)

      then:
        result == true
  }
}