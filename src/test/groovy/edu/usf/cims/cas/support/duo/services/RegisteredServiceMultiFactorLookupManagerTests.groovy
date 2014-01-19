package edu.usf.cims.cas.support.duo.services

import spock.lang.Specification
import net.unicon.cas.addons.serviceregistry.RegisteredServiceWithAttributesImpl
import org.jasig.cas.authentication.principal.Principal

class RegisteredServiceMultiFactorLookupManagerTests extends Specification {

  def principal = Mock(Principal)

  def "Access a service that does not have a MFA value"(){
      given:
        principal.id >> "testUser"

        def service = new RegisteredServiceWithAttributesImpl()
        service.serviceId = "Test-NoMFASetting"

      when:
        def lookupManager = new RegisteredServiceMultiFactorLookupManager()
        def result = lookupManager.getMFARequired(service,principal)

      then:
        result == false
    }

  def "Access a service that requires MFA for all users"(){
      given:
        def multiFactorRequiredAttributeName = "casMFARequired"
        def multiFactorRequiredAttributeValue = "ALL"
        principal.id >> "testUser"

        def service = new RegisteredServiceWithAttributesImpl()
        service.serviceId = "Test-MFARequired"
        service.extraAttributes = [(multiFactorRequiredAttributeName) : multiFactorRequiredAttributeValue]

      when:
        def lookupManager = new RegisteredServiceMultiFactorLookupManager()
        def result = lookupManager.getMFARequired(service,principal)

      then:
        result == true
    }

    def "Access a service that does not require MFA"(){
      given:
        def multiFactorRequiredAttributeName = "casMFARequired"
        def multiFactorRequiredAttributeValue = "NONE"
        principal.id >> "testUser"

        def service = new RegisteredServiceWithAttributesImpl()
        service.serviceId = "Test-NoMFARequired"
        service.extraAttributes = [(multiFactorRequiredAttributeName) : multiFactorRequiredAttributeValue]

      when:
        def lookupManager = new RegisteredServiceMultiFactorLookupManager()
        def result = lookupManager.getMFARequired(service,principal)

      then:
        result == false
    }

  def "Access a service that requires MFA for some users"(){
      given:
        def multiFactorRequiredAttributeName = "casMFARequired"
        def multiFactorRequiredAttributeValue = "CHECK_ATTRIBUTE"
        def multiFactorRequiredAttributeMapAttributeName = "casMFAUserAttributes"
        def multiFactorRequiredAttributeMapAttributeValue = [uid: ["testUser", "foo", "bar"]]
        principal.id >> "testUser"
        principal.attributes >> [uid: "testUser"]

        def service = new RegisteredServiceWithAttributesImpl()
        service.serviceId = "Test-MFARequiredList"
        service.extraAttributes = [ (multiFactorRequiredAttributeName) : multiFactorRequiredAttributeValue,
                                    (multiFactorRequiredAttributeMapAttributeName) : multiFactorRequiredAttributeMapAttributeValue]
      when:
        def lookupManager = new RegisteredServiceMultiFactorLookupManager()
        def result = lookupManager.getMFARequired(service,principal)

      then:
        result == true
    }

  def "Access a service that requires MFA for some users - alternate attribute names"(){
      given:
        def multiFactorRequiredAttributeName = "requireTwoFactor"
        def multiFactorRequiredAttributeValue = "CHECK_ATTRIBUTE"
        def multiFactorRequiredAttributeMapAttributeName = "DuoAttributes"
        def multiFactorRequiredAttributeMapAttributeValue = [attributeOne: ["foo", "bar"]]
        principal.id >> "testUser"
        principal.attributes >> [attributeOne: ["bar", "baz"]]

        def service = new RegisteredServiceWithAttributesImpl()
        service.serviceId = "Test-MFARequiredList-AltNames"
        service.extraAttributes = [ (multiFactorRequiredAttributeName) : multiFactorRequiredAttributeValue,
                                    (multiFactorRequiredAttributeMapAttributeName) : multiFactorRequiredAttributeMapAttributeValue]

      when:
        def lookupManager = new RegisteredServiceMultiFactorLookupManager()
        lookupManager.setMultiFactorRequiredAttributeName(multiFactorRequiredAttributeName)
        lookupManager.setMultiFactorRequiredAttributeMapAttributeName(multiFactorRequiredAttributeMapAttributeName)
        def result = lookupManager.getMFARequired(service,principal)

      then:
        result == true
    }

  def "Access a service that requires MFA for some users and the current user does not meet the criteria"(){
      given:
        def multiFactorRequiredAttributeName = "casMFARequired"
        def multiFactorRequiredAttributeValue = "CHECK_ATTRIBUTE"
        def multiFactorRequiredAttributeMapAttributeName = "casMFAUserAttributes"
        def multiFactorRequiredAttributeMapAttributeValue = [attributeOne: ["foo", "bar"]]
        principal.id >> "testUser"
        principal.attributes >> [attributeTwo: ["bar", "baz"]]

        def service = new RegisteredServiceWithAttributesImpl()
        service.serviceId = "Test-MFARequiredList-NotRequired"
        service.extraAttributes = [ (multiFactorRequiredAttributeName) : multiFactorRequiredAttributeValue,
                                    (multiFactorRequiredAttributeMapAttributeName) : multiFactorRequiredAttributeMapAttributeValue]

      when:
        def lookupManager = new RegisteredServiceMultiFactorLookupManager()
        def result = lookupManager.getMFARequired(service,principal)

      then:
        result == false
    }

  def "Access a service that has an invalid requirement value"(){
      given:
        def multiFactorRequiredAttributeName = "casMFARequired"
        def multiFactorRequiredAttributeValue = "YES"
        def multiFactorRequiredAttributeMapAttributeName = "casMFAUserAttributes"
        def multiFactorRequiredAttributeMapAttributeValue = [attributeOne: ["foo", "bar"]]
        principal.id >> "testUser"
        principal.attributes >> [attributeOne: ["bar", "baz"]]

        def service = new RegisteredServiceWithAttributesImpl()
        service.serviceId = "Test-InvalidMFA"
        service.extraAttributes = [ (multiFactorRequiredAttributeName) : multiFactorRequiredAttributeValue,
                                    (multiFactorRequiredAttributeMapAttributeName) : multiFactorRequiredAttributeMapAttributeValue]

      when:
        def lookupManager = new RegisteredServiceMultiFactorLookupManager()
        def result = lookupManager.getMFARequired(service,principal)

      then:
        result == false
    }
}