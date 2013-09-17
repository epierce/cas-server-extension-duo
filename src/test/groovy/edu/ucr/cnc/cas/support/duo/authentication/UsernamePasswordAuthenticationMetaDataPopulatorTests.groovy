package edu.ucr.cnc.cas.support.duo.authentication

import spock.lang.Specification
import org.jasig.cas.authentication.principal.SimplePrincipal
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials
import edu.ucr.cnc.cas.support.duo.authentication.principal.DuoCredentials
import org.jasig.cas.authentication.MutableAuthentication
import edu.ucr.cnc.cas.support.duo.CasConstants


class UsernamePasswordAuthenticationMetaDataPopulatorTests extends Specification {

  def "Populate Single Factor LOA in TGT"(){
    given:
      def credentials = new UsernamePasswordCredentials()
      def principal = new SimplePrincipal("testUser")
      def mutableAuth = new MutableAuthentication(principal)

    when:
      def populator = new UsernamePasswordAuthenticationMetaDataPopulator()
      def finalAuth = populator.populateAttributes(mutableAuth, credentials)

    then:
      finalAuth.getAttributes().get(CasConstants.LOA_ATTRIBUTE) == CasConstants.LOA_SF
  }

  def "Only populate Single Factor LOA if its a UsernamePasswordCredentials"(){
    given:
      def credentials = new DuoCredentials()
      def principal = new SimplePrincipal("testUser")
      def mutableAuth = new MutableAuthentication(principal)

    when:
      def populator = new UsernamePasswordAuthenticationMetaDataPopulator()
      def finalAuth = populator.populateAttributes(mutableAuth, credentials)

    then:
      finalAuth.getAttributes().get(CasConstants.LOA_ATTRIBUTE) == null
  }
}