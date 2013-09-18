package edu.ucr.cnc.cas.support.duo.web.flow

import spock.lang.Specification
import net.unicon.cas.addons.serviceregistry.RegisteredServiceWithAttributesImpl
import org.jasig.cas.authentication.principal.WebApplicationService
import org.jasig.cas.services.ServicesManager
import org.jasig.cas.ticket.registry.TicketRegistry
import org.jasig.cas.authentication.principal.Principal
import org.jasig.cas.authentication.Authentication
import org.jasig.cas.ticket.TicketGrantingTicket
import org.jasig.cas.ticket.registry.TicketRegistry
import edu.ucr.cnc.cas.support.duo.services.ServiceMultiFactorLookupManager
import edu.ucr.cnc.cas.support.duo.CasConstants

import org.springframework.webflow.execution.RequestContext
import org.springframework.webflow.core.collection.LocalAttributeMap

class CheckLoaOfTicketGrantingTicketTests extends Specification {

  /**
   * Mock objects that will be used in all tests
   */
  def sfa_principal = Mock(Principal)
  def mfa_principal = Mock(Principal)
  def sfa_authentication = Mock(Authentication)
  def mfa_authentication = Mock(Authentication)
  def sfa_tgt = Mock(TicketGrantingTicket)
  def mfa_tgt = Mock(TicketGrantingTicket)
  def ticketRegistry = Mock(TicketRegistry)
  def webApplicationService = Mock(WebApplicationService)
  def requestContext = Mock(RequestContext)
  def servicesManager = Mock(ServicesManager)
  def serviceMultiFactorLookupManager = Mock(ServiceMultiFactorLookupManager)

  /**
  * configure mocked objects
  **/
  def setup() {

    ticketRegistry.getTicket('test-tgt-sfa', TicketGrantingTicket) >> sfa_tgt
    sfa_tgt.authentication >> sfa_authentication
    sfa_authentication.principal >> sfa_principal
    sfa_principal.id  >> "testUser1"
    sfa_authentication.attributes >> [(CasConstants.LOA_ATTRIBUTE): CasConstants.LOA_SF]

    ticketRegistry.getTicket('test-tgt-mfa', TicketGrantingTicket) >> mfa_tgt
    mfa_tgt.authentication >> mfa_authentication
    mfa_authentication.principal >> mfa_principal
    mfa_principal.id  >> "testUser2"
    mfa_authentication.attributes >> [(CasConstants.LOA_ATTRIBUTE): CasConstants.LOA_TF]
    
    //Service URL the user is accessing
    webApplicationService.id >> "http://example.com/service"

  }

  def "Single Factor Login: require MFA"(){
    given:
      //Configure the mocked web request
      requestContext.getFlowScope() >> new LocalAttributeMap([ticketGrantingTicketId: 'test-tgt-sfa', service: webApplicationService])

      //Configure the mocked serviceLookup
      serviceMultiFactorLookupManager.getMFARequired(_ , _) >> true

      def loaCheck = new CheckLoaOfTicketGrantingTicket()
      loaCheck.serviceMultiFactorLookupManager = serviceMultiFactorLookupManager
      loaCheck.servicesManager = servicesManager
      loaCheck.ticketRegistry = ticketRegistry

    when:
      def result = loaCheck.doExecute(requestContext)

    then:
      result.toString() == 'renewForTwoFactor'
  }

  def "Single Factor Login: do not require MFA"(){
    given:
      //Configure the mocked web request
      requestContext.getFlowScope() >> new LocalAttributeMap([ticketGrantingTicketId: 'test-tgt-sfa', service: webApplicationService])

      //Configure the mocked serviceLookup
      serviceMultiFactorLookupManager.getMFARequired(_ , _) >> false

      def loaCheck = new CheckLoaOfTicketGrantingTicket()
      loaCheck.serviceMultiFactorLookupManager = serviceMultiFactorLookupManager
      loaCheck.servicesManager = servicesManager
      loaCheck.ticketRegistry = ticketRegistry

    when:
      def result = loaCheck.doExecute(requestContext)

    then:
      result.toString() == 'continue'
  }

  def "MultiFactor Login: meets service requirements"(){
    given:
      //Configure the mocked web request
      requestContext.getFlowScope() >> new LocalAttributeMap([ticketGrantingTicketId: 'test-tgt-mfa', service: webApplicationService])

      //Configure the mocked serviceLookup
      serviceMultiFactorLookupManager.getMFARequired(_ , _) >> true

      def loaCheck = new CheckLoaOfTicketGrantingTicket()
      loaCheck.serviceMultiFactorLookupManager = serviceMultiFactorLookupManager
      loaCheck.servicesManager = servicesManager
      loaCheck.ticketRegistry = ticketRegistry

    when:
      def result = loaCheck.doExecute(requestContext)

    then:
      result.toString() == 'continue'
  }

  def "MultiFactor Login: service only requires single factor"(){
    given:
      //Configure the mocked web request
      requestContext.getFlowScope() >> new LocalAttributeMap([ticketGrantingTicketId: 'test-tgt-mfa', service: webApplicationService])

      //Configure the mocked serviceLookup
      serviceMultiFactorLookupManager.getMFARequired(_ , _) >> false

      def loaCheck = new CheckLoaOfTicketGrantingTicket()
      loaCheck.serviceMultiFactorLookupManager = serviceMultiFactorLookupManager
      loaCheck.servicesManager = servicesManager
      loaCheck.ticketRegistry = ticketRegistry

    when:
      def result = loaCheck.doExecute(requestContext)

    then:
      result.toString() == 'continue'
  }
}