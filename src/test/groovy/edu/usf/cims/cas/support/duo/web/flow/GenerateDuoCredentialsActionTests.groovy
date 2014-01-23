package edu.usf.cims.cas.support.duo.web.flow

import spock.lang.Specification
import net.unicon.cas.addons.serviceregistry.RegisteredServiceWithAttributesImpl
import org.jasig.cas.authentication.principal.WebApplicationService
import org.jasig.cas.services.ServicesManager
import org.jasig.cas.ticket.registry.TicketRegistry
import org.jasig.cas.ticket.TicketGrantingTicket
import org.jasig.cas.authentication.principal.Principal
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.authentication.Authentication
import edu.ucr.cnc.cas.support.duo.CasConstants
import edu.usf.cims.cas.support.duo.authentication.principal.DuoCredentials
import org.springframework.webflow.execution.RequestContext
import org.springframework.webflow.core.collection.LocalAttributeMap

class GenerateDuoCredentialsActionTests extends Specification {

  /**
   * Mock objects that will be used in all tests
   */
  def sfa_principal = Mock(Principal)
  def sfa_authentication = Mock(Authentication)
  def sfa_tgt = Mock(TicketGrantingTicket)
  def requestContext = Mock(RequestContext)
  def ticketRegistry = Mock(TicketRegistry)

  /**
  * configure mocked objects
  **/
  def setup() {

    sfa_authentication.principal >> sfa_principal
    sfa_principal.id  >> "testUser"
    sfa_authentication.attributes >> [(CasConstants.LOA_ATTRIBUTE): CasConstants.LOA_SF]



    ticketRegistry.getTicket('test-tgt-sfa', TicketGrantingTicket) >> sfa_tgt
    sfa_tgt.authentication >> sfa_authentication
    sfa_authentication.principal >> sfa_principal
    sfa_principal.id  >> "testUser"
    sfa_authentication.attributes >> [(CasConstants.LOA_ATTRIBUTE): CasConstants.LOA_SF]
  }

  def "Create a new DuoCredential during intial login"(){
    given:

      def credentials = new UsernamePasswordCredentials()
      credentials.username = "testUser"

      //Configure the mocked web request
      requestContext.getFlowScope() >> new LocalAttributeMap([credentials: credentials])
      requestContext.getRequestScope() >> new LocalAttributeMap([ticketGrantingTicketId: 'test-tgt-sfa'])

      def action = new GenerateDuoCredentialsAction()
      action.ticketRegistry = ticketRegistry
    when:
      def result = action.createDuoCredentials(requestContext)
      def resultCredential = requestContext.flowScope.duoCredentials

    then:
      result == "created"
      resultCredential instanceof DuoCredentials
      resultCredential.getPrincipal().getId() == "testUser"
  }

  def "Create a new DuoCredential after initial login with a valid CAS session"(){
    given:
      //Configure the mocked web request
      requestContext.getFlowScope() >> new LocalAttributeMap([ticketGrantingTicketId: 'test-tgt-sfa'])
      requestContext.getRequestScope() >> new LocalAttributeMap([:])

      def action = new GenerateDuoCredentialsAction()
      action.ticketRegistry = ticketRegistry
    when:
      def result = action.createDuoCredentials(requestContext)
      def resultCredential = requestContext.flowScope.duoCredentials

    then:
      result == "created"
      resultCredential instanceof DuoCredentials
      resultCredential.getPrincipal().getId() == "testUser"
  }

  def "Create a new DuoCredential after CAS session has timed-out and user reauthenticated"(){
    given:

      def credentials = new UsernamePasswordCredentials()
      credentials.username = "testUser"

      //Configure the mocked web request
      requestContext.getFlowScope() >> new LocalAttributeMap([credentials: credentials, ticketGrantingTicketId: 'test-tgt-sfa-old'])
      requestContext.getRequestScope() >> new LocalAttributeMap([ticketGrantingTicketId: 'test-tgt-sfa'])

      def action = new GenerateDuoCredentialsAction()
      action.ticketRegistry = ticketRegistry
    when:
      def result = action.createDuoCredentials(requestContext)
      def resultCredential = requestContext.flowScope.duoCredentials

    then:
      result == "created"
      resultCredential instanceof DuoCredentials
      resultCredential.getPrincipal().getId() == "testUser"
  }

  def "Do not create a DuoCredential when there is no TGTid"(){
    given:
      def credentials = new UsernamePasswordCredentials()
      credentials.username = "testUser"

      //Configure the mocked web request
      requestContext.getFlowScope() >> new LocalAttributeMap([credentials: credentials])
      requestContext.getRequestScope() >> new LocalAttributeMap([:])

      def action = new GenerateDuoCredentialsAction()
      action.ticketRegistry = ticketRegistry
    when:
      def result = action.createDuoCredentials(requestContext)
      def resultCredential = requestContext.flowScope.duoCredentials

    then:
      result == "error"
  }

  def "Do not create a DuoCredential when there is no first-factor authentication"(){
    given:
      def credentials = new UsernamePasswordCredentials()
      credentials.username = "testUser"

      //Configure the mocked web request
      requestContext.getFlowScope() >> new LocalAttributeMap([credentials: credentials])
      requestContext.getRequestScope() >> new LocalAttributeMap([ticketGrantingTicketId: 'test-tgt-not-found'])

      def action = new GenerateDuoCredentialsAction()
      action.ticketRegistry = ticketRegistry
    when:
      def result = action.createDuoCredentials(requestContext)
      def resultCredential = requestContext.flowScope.duoCredentials

    then:
      result == "error"
  }
}