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
        def mfaRequiredKey = "casMFARequired"
        def mfaRequiredValue = "ALL"
        principal.id >> "testUser"

        def service = new RegisteredServiceWithAttributesImpl()
        service.serviceId = "Test-MFARequired"
        service.extraAttributes = [(mfaRequiredKey) : mfaRequiredValue]

      when:
        def lookupManager = new RegisteredServiceMultiFactorLookupManager()
        def result = lookupManager.getMFARequired(service,principal)

      then:
        result == true
    }

    def "Access a service that does not require MFA"(){
      given:
        def mfaRequiredKey = "casMFARequired"
        def mfaRequiredValue = "NONE"
        principal.id >> "testUser"

        def service = new RegisteredServiceWithAttributesImpl()
        service.serviceId = "Test-NoMFARequired"
        service.extraAttributes = [(mfaRequiredKey) : mfaRequiredValue]

      when:
        def lookupManager = new RegisteredServiceMultiFactorLookupManager()
        def result = lookupManager.getMFARequired(service,principal)

      then:
        result == false
    }

  def "Access a service that requires MFA for some users"(){
      given:
        def mfaRequiredKey = "casMFARequired"
        def mfaRequiredValue = "CHECK_ATTRIBUTE"
        def mfaRequiredAttributesKey = "casMFAUserAttributes"
        def mfaRequiredAttributesValue = [uid: ["testUser", "foo", "bar"]]
        principal.id >> "testUser"
        principal.attributes >> [uid: "testUser"]

        def service = new RegisteredServiceWithAttributesImpl()
        service.serviceId = "Test-MFARequiredList"
        service.extraAttributes = [ (mfaRequiredKey) : mfaRequiredValue, (mfaRequiredAttributesKey) : mfaRequiredAttributesValue]
      when:
        def lookupManager = new RegisteredServiceMultiFactorLookupManager()
        def result = lookupManager.getMFARequired(service,principal)

      then:
        result == true
    }

  def "Access a service that requires MFA for some users - alternate attribute names"(){
      given:
        def mfaRequiredKey = "requireTwoFactor"
        def mfaRequiredValue = "CHECK_ATTRIBUTE"
        def mfaRequiredAttributesKey = "DuoAttributes"
        def mfaRequiredAttributesValue = [attributeOne: ["foo", "bar"]]
        principal.id >> "testUser"
        principal.attributes >> [attributeOne: ["bar", "baz"]]

        def service = new RegisteredServiceWithAttributesImpl()
        service.serviceId = "Test-MFARequiredList-AltNames"
        service.extraAttributes = [ (mfaRequiredKey) : mfaRequiredValue, (mfaRequiredAttributesKey) : mfaRequiredAttributesValue]

      when:
        def lookupManager = new RegisteredServiceMultiFactorLookupManager()
        lookupManager.setMfaRequiredKey(mfaRequiredKey)
        lookupManager.setMfaRequiredAttributesKey(mfaRequiredAttributesKey)
        def result = lookupManager.getMFARequired(service,principal)

      then:
        result == true
    }

  def "Access a service that requires MFA for some users and the current user does not meet the criteria"(){
      given:
        def mfaRequiredKey = "casMFARequired"
        def mfaRequiredValue = "CHECK_ATTRIBUTE"
        def mfaRequiredAttributesKey = "casMFAUserAttributes"
        def mfaRequiredAttributesValue = [attributeOne: ["foo", "bar"]]
        principal.id >> "testUser"
        principal.attributes >> [attributeTwo: ["bar", "baz"]]

        def service = new RegisteredServiceWithAttributesImpl()
        service.serviceId = "Test-MFARequiredList-NotRequired"
        service.extraAttributes = [ (mfaRequiredKey) : mfaRequiredValue, (mfaRequiredAttributesKey) : mfaRequiredAttributesValue]

      when:
        def lookupManager = new RegisteredServiceMultiFactorLookupManager()
        def result = lookupManager.getMFARequired(service,principal)

      then:
        result == false
    }

  def "Access a service that has an invalid requirement value"(){
      given:
        def mfaRequiredKey = "casMFARequired"
        def mfaRequiredValue = "YES"
        def mfaRequiredAttributesKey = "casMFAUserAttributes"
        def mfaRequiredAttributesValue = [attributeOne: ["foo", "bar"]]
        principal.id >> "testUser"
        principal.attributes >> [attributeOne: ["bar", "baz"]]

        def service = new RegisteredServiceWithAttributesImpl()
        service.serviceId = "Test-InvalidMFA"
        service.extraAttributes = [ (mfaRequiredKey) : mfaRequiredValue, (mfaRequiredAttributesKey) : mfaRequiredAttributesValue]

      when:
        def lookupManager = new RegisteredServiceMultiFactorLookupManager()
        def result = lookupManager.getMFARequired(service,principal)

      then:
        result == false
    }
}