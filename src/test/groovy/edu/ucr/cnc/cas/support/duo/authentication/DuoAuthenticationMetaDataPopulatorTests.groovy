package edu.ucr.cnc.cas.support.duo.authentication

import spock.lang.Specification
import org.jasig.cas.authentication.principal.SimplePrincipal
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials
import edu.ucr.cnc.cas.support.duo.authentication.principal.DuoCredentials
import org.jasig.cas.authentication.MutableAuthentication
import edu.ucr.cnc.cas.support.duo.CasConstants


class DuoAuthenticationMetaDataPopulatorTests extends Specification {

  def "Populate TwoFactor LOA in TGT"(){
    given:
      def credentials = new DuoCredentials()
      def principal = new SimplePrincipal("testUser")
      def mutableAuth = new MutableAuthentication(principal)

    when:
      def populator = new DuoAuthenticationMetaDataPopulator()
      def finalAuth = populator.populateAttributes(mutableAuth, credentials)

    then:
      finalAuth.getAttributes().get(CasConstants.LOA_ATTRIBUTE) == CasConstants.LOA_TF
  }

  def "Only populate the LOA if its a DuoCredential"(){
    given:
      def credentials = new UsernamePasswordCredentials()
      def principal = new SimplePrincipal("testUser")
      def mutableAuth = new MutableAuthentication(principal)

    when:
      def populator = new DuoAuthenticationMetaDataPopulator()
      def finalAuth = populator.populateAttributes(mutableAuth, credentials)

    then:
      finalAuth.getAttributes().get(CasConstants.LOA_ATTRIBUTE) == null
  }
}