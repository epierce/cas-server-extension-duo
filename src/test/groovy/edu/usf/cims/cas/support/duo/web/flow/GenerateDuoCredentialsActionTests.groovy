package edu.usf.cims.cas.support.duo.web.flow

import spock.lang.Specification
import net.unicon.cas.addons.serviceregistry.RegisteredServiceWithAttributesImpl
import org.jasig.cas.authentication.principal.WebApplicationService
import org.jasig.cas.services.ServicesManager
import org.jasig.cas.ticket.registry.TicketRegistry
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
  def requestContext = Mock(RequestContext)
  
  /**
  * configure mocked objects
  **/
  def setup() {

    sfa_authentication.principal >> sfa_principal
    sfa_principal.id  >> "testUser"
    sfa_authentication.attributes >> [(CasConstants.LOA_ATTRIBUTE): CasConstants.LOA_SF]

    def credentials = new UsernamePasswordCredentials()
    credentials.username = "testUser"

    //Configure the mocked web request
    requestContext.getFlowScope() >> new LocalAttributeMap([credentials: credentials, casAuthentication: sfa_authentication])

  }

  def "Create a new DuoCredential"(){
    given:
      def action = new GenerateDuoCredentialsAction()
    when:
      def result = action.createDuoCredentials(requestContext)

    then:
      result instanceof DuoCredentials
      result.getPrincipal().getId() == "testUser"
  }
}