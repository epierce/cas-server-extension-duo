package edu.usf.cims.cas.support.duo.services

import spock.lang.Specification
import net.unicon.cas.addons.serviceregistry.RegisteredServiceWithAttributesImpl
import org.jasig.cas.authentication.principal.Principal

class JsonServiceMultiFactorLookupManagerTests extends Specification {

  def principal = Mock(Principal)

  def "Access a service that does not have a MFA value"(){
      given:
        principal.id >> "testUser"

        def service = new RegisteredServiceWithAttributesImpl()
        service.serviceId = "Test-NoMFASetting"

      when:
        def lookupManager = new JsonServiceMultiFactorLookupManager()
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
        def lookupManager = new JsonServiceMultiFactorLookupManager()
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
        def lookupManager = new JsonServiceMultiFactorLookupManager()
        def result = lookupManager.getMFARequired(service,principal)

      then:
        result == false
    }

  def "Access a service that requires MFA for some users"(){
      given:
        def multiFactorRequiredAttributeName = "casMFARequired"
        def multiFactorRequiredAttributeValue = "USER_LIST"
        def multiFactorRequiredUserListAttributeName = "casMFARequiredUsers"
        def multiFactorRequiredUserListAttributeValue = ["testUser","foo","bar"]
        principal.id >> "testUser"

        def service = new RegisteredServiceWithAttributesImpl()
        service.serviceId = "Test-MFARequiredList"
        service.extraAttributes = [ (multiFactorRequiredAttributeName) : multiFactorRequiredAttributeValue,
                                    (multiFactorRequiredUserListAttributeName) : multiFactorRequiredUserListAttributeValue]

      when:
        def lookupManager = new JsonServiceMultiFactorLookupManager()
        def result = lookupManager.getMFARequired(service,principal)

      then:
        result == true
    }

  def "Access a service that requires MFA for some users - alternate attribute names"(){
      given:
        def multiFactorRequiredAttributeName = "requireTwoFactor"
        def multiFactorRequiredAttributeValue = "USER_LIST"
        def multiFactorRequiredUserListAttributeName = "TwoFactorUsers"
        def multiFactorRequiredUserListAttributeValue = ["testUser","foo","bar"]
        principal.id >> "testUser"

        def service = new RegisteredServiceWithAttributesImpl()
        service.serviceId = "Test-MFARequiredList-AltNames"
        service.extraAttributes = [ (multiFactorRequiredAttributeName) : multiFactorRequiredAttributeValue,
                                    (multiFactorRequiredUserListAttributeName) : multiFactorRequiredUserListAttributeValue]

      when:
        def lookupManager = new JsonServiceMultiFactorLookupManager()
        lookupManager.setMultiFactorRequiredAttributeName(multiFactorRequiredAttributeName)
        lookupManager.setMultiFactorRequiredUserListAttributeName(multiFactorRequiredUserListAttributeName)
        def result = lookupManager.getMFARequired(service,principal)

      then:
        result == true
    }

  def "Access a service that requires MFA for some users and the current user is not in the list"(){
      given:
        def multiFactorRequiredAttributeName = "casMFARequired"
        def multiFactorRequiredAttributeValue = "USER_LIST"
        def multiFactorRequiredUserListAttributeName = "casMFARequiredUsers"
        def multiFactorRequiredUserListAttributeValue = ["baz","foo","bar"]
        principal.id >> "testUser"

        def service = new RegisteredServiceWithAttributesImpl()
        service.serviceId = "Test-MFARequiredList-NotRequired"
        service.extraAttributes = [ (multiFactorRequiredAttributeName) : multiFactorRequiredAttributeValue,
                                    (multiFactorRequiredUserListAttributeName) : multiFactorRequiredUserListAttributeValue]

      when:
        def lookupManager = new JsonServiceMultiFactorLookupManager()
        def result = lookupManager.getMFARequired(service,principal)

      then:
        result == false
    }

  def "Access a service that has an invalid requirement value"(){
      given:
        def multiFactorRequiredAttributeName = "casMFARequired"
        def multiFactorRequiredAttributeValue = "YES"
        def multiFactorRequiredUserListAttributeName = "casMFARequiredUsers"
        def multiFactorRequiredUserListAttributeValue = ["baz","foo","bar"]
        principal.id >> "testUser"

        def service = new RegisteredServiceWithAttributesImpl()
        service.serviceId = "Test-InvalidMFA"
        service.extraAttributes = [ (multiFactorRequiredAttributeName) : multiFactorRequiredAttributeValue,
                                    (multiFactorRequiredUserListAttributeName) : multiFactorRequiredUserListAttributeValue]

      when:
        def lookupManager = new JsonServiceMultiFactorLookupManager()
        def result = lookupManager.getMFARequired(service,principal)

      then:
        result == false
    }
}