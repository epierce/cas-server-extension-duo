package edu.ucr.cnc.cas.support.duo.web.flow

import spock.lang.Specification
import org.jasig.cas.mock.MockTicketGrantingTicket
import edu.ucr.cnc.cas.support.duo.services.JsonServiceMultiFactorLookupManager

class CheckLoaOfTicketGrantingTicketTests extends Specification {

  def "Check the LOA of a TGT - require MFA"(){
    given:
      def username = "testUser"
      def lookupManager = new JsonServiceMultiFactorLookupManager()
      def ticketGrantingTicket = new MockTicketGrantingTicket(username)
      def service  

    when:

    then:
  }
}