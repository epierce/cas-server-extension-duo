package edu.ucr.cnc.cas.support.duo.web.flow

import spock.lang.Specification
import org.jasig.cas.authentication.principal.WebApplicationService
import org.jasig.cas.services.ServicesManager
import org.jasig.cas.services.UnauthorizedServiceException
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials
import org.jasig.cas.authentication.principal.Principal
import org.jasig.cas.authentication.Authentication
import org.jasig.cas.ticket.TicketGrantingTicket
import edu.ucr.cnc.cas.support.duo.services.ServiceMultiFactorLookupManager
import edu.ucr.cnc.cas.support.duo.authentication.principal.UserMultiFactorLookupManager
import net.unicon.cas.addons.serviceregistry.RegisteredServiceWithAttributesImpl
import org.springframework.webflow.execution.RequestContext
import org.springframework.webflow.core.collection.LocalAttributeMap

class DetermineIfTwoFactorActionTests extends Specification {

  /**
   * Mock objects that will be used in all tests
   */
  def authentication = Mock(Authentication)
  def webApplicationService = Mock(WebApplicationService)
  def requestContext = Mock(RequestContext)
  def servicesManager = Mock(ServicesManager)
  def serviceMultiFactorLookupManager = Mock(ServiceMultiFactorLookupManager)
  def userMultiFactorLookupManager = Mock(UserMultiFactorLookupManager)

  /**
  * configure mocked objects
  **/
  def setup() {

    def credentials = new UsernamePasswordCredentials()
    credentials.username = "testUser"

    //Service URL the user is accessing
    webApplicationService.id >> "http://example.com/service"
    requestContext.getFlowScope() >> new LocalAttributeMap([credentials: credentials])
  }

  def "Either ServiceLookupManager or UserLookupManager are required"(){
    given:
      def registeredService = new RegisteredServiceWithAttributesImpl()
      registeredService.extraAttributes = [casMFARequired: "ALL"]
      registeredService.serviceId = "http://example.com/service"

      //Configure the mocked service registry to return this service
      servicesManager.findServiceBy(_) >> registeredService

    when:
      def determineIfTwoFactor = new DetermineIfTwoFactorAction()
      def result = determineIfTwoFactor.doExecute(requestContext)

    then:
      thrown(java.lang.IllegalArgumentException)
  }

  def "service requires MFA (no UserLookupManager)"(){
    given:
      def registeredService = new RegisteredServiceWithAttributesImpl()
      registeredService.extraAttributes = [casMFARequired: "ALL"]
      registeredService.serviceId = "http://example.com/service"

      //Configure the mocked service registry to return this service
      servicesManager.findServiceBy(_) >> registeredService

      serviceMultiFactorLookupManager.getMFARequired(_,_) >> true

    when:
      def determineIfTwoFactor = new DetermineIfTwoFactorAction()
      determineIfTwoFactor.servicesManager = servicesManager
      determineIfTwoFactor.serviceMultiFactorLookupManager = serviceMultiFactorLookupManager
      def result = determineIfTwoFactor.doExecute(requestContext)

    then:
      result.toString() == "multiFactorNeeded"
  }

  def "service does not require MFA (no UserLookupManager)"(){
    given:
      def registeredService = new RegisteredServiceWithAttributesImpl()
      registeredService.extraAttributes = [casMFARequired: "ALL"]
      registeredService.serviceId = "http://example.com/service"

      //Configure the mocked service registry to return this service
      servicesManager.findServiceBy(_) >> registeredService

      serviceMultiFactorLookupManager.getMFARequired(_,_) >> false

    when:
      def determineIfTwoFactor = new DetermineIfTwoFactorAction()
      determineIfTwoFactor.servicesManager = servicesManager
      determineIfTwoFactor.serviceMultiFactorLookupManager = serviceMultiFactorLookupManager
      def result = determineIfTwoFactor.doExecute(requestContext)

    then:
      result.toString() == "noMultiFactorNeeded"
  }

  def "Service requires MFA, UserLookupManager does not require it"(){
    given:
      def registeredService = new RegisteredServiceWithAttributesImpl()
      registeredService.extraAttributes = [casMFARequired: "ALL"]
      registeredService.serviceId = "http://example.com/service"

      //Configure the mocked service registry to return this service
      servicesManager.findServiceBy(_) >> registeredService

      serviceMultiFactorLookupManager.getMFARequired(_,_) >> true
      userMultiFactorLookupManager.getMFARequired(_) >> false

    when:
      def determineIfTwoFactor = new DetermineIfTwoFactorAction()
      determineIfTwoFactor.servicesManager = servicesManager
      determineIfTwoFactor.serviceMultiFactorLookupManager = serviceMultiFactorLookupManager
      determineIfTwoFactor.userMultiFactorLookupManager = userMultiFactorLookupManager
      def result = determineIfTwoFactor.doExecute(requestContext)

    then:
      result.toString() == "multiFactorNeeded"
  }

  def "Service and user require MFA"(){
    given:
      def registeredService = new RegisteredServiceWithAttributesImpl()
      registeredService.extraAttributes = [casMFARequired: "ALL"]
      registeredService.serviceId = "http://example.com/service"

      //Configure the mocked service registry to return this service
      servicesManager.findServiceBy(_) >> registeredService

      serviceMultiFactorLookupManager.getMFARequired(_,_) >> true
      userMultiFactorLookupManager.getMFARequired(_) >> true

    when:
      def determineIfTwoFactor = new DetermineIfTwoFactorAction()
      determineIfTwoFactor.servicesManager = servicesManager
      determineIfTwoFactor.serviceMultiFactorLookupManager = serviceMultiFactorLookupManager
      determineIfTwoFactor.userMultiFactorLookupManager = userMultiFactorLookupManager
      def result = determineIfTwoFactor.doExecute(requestContext)

    then:
      result.toString() == "multiFactorNeeded"
  }

  def "Service does not require MFA, but user does"(){
    given:
      def registeredService = new RegisteredServiceWithAttributesImpl()
      registeredService.extraAttributes = [casMFARequired: "ALL"]
      registeredService.serviceId = "http://example.com/service"

      //Configure the mocked service registry to return this service
      servicesManager.findServiceBy(_) >> registeredService

      serviceMultiFactorLookupManager.getMFARequired(_,_) >> false
      userMultiFactorLookupManager.getMFARequired(_) >> true

    when:
      def determineIfTwoFactor = new DetermineIfTwoFactorAction()
      determineIfTwoFactor.servicesManager = servicesManager
      determineIfTwoFactor.serviceMultiFactorLookupManager = serviceMultiFactorLookupManager
      determineIfTwoFactor.userMultiFactorLookupManager = userMultiFactorLookupManager
      def result = determineIfTwoFactor.doExecute(requestContext)

    then:
      result.toString() == "multiFactorNeeded"
  }

  def "Service and user do not require MFA"(){
    given:
      def registeredService = new RegisteredServiceWithAttributesImpl()
      registeredService.extraAttributes = [casMFARequired: "ALL"]
      registeredService.serviceId = "http://example.com/service"

      //Configure the mocked service registry to return this service
      servicesManager.findServiceBy(_) >> registeredService

      serviceMultiFactorLookupManager.getMFARequired(_,_) >> false
      userMultiFactorLookupManager.getMFARequired(_) >> false

    when:
      def determineIfTwoFactor = new DetermineIfTwoFactorAction()
      determineIfTwoFactor.servicesManager = servicesManager
      determineIfTwoFactor.serviceMultiFactorLookupManager = serviceMultiFactorLookupManager
      determineIfTwoFactor.userMultiFactorLookupManager = userMultiFactorLookupManager
      def result = determineIfTwoFactor.doExecute(requestContext)

    then:
      result.toString() == "noMultiFactorNeeded"
  }
}