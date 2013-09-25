package edu.usf.cims.cas.support.duo.authentication.principal

import spock.lang.Specification
import com.duosecurity.*
import edu.ucr.cnc.cas.support.duo.DuoConfiguration
import edu.usf.cims.cas.support.duo.authentication.principal.DuoCredentials
import edu.usf.cims.cas.support.duo.authentication.principal.DuoCredentialsToPrincipalResolver
import edu.ucr.cnc.cas.support.duo.CasConstants


class DuoCredentialsToPrincipalResolverTests extends Specification {

  def "Get principal from DuoCredentials"(){
    given:

      def IKEY = "DIXXXXXXXXXXXXXXXXXX"
      def SKEY = "deadbeefdeadbeefdeadbeefdeadbeefdeadbeef"
      def AKEY = "useacustomerprovidedapplicationsecretkey"
      def USER = "testuser"
      def RESPONSE = "AUTH|dGVzdHVzZXJ8RElYWFhYWFhYWFhYWFhYWFhYWFh8MTYxNTcyNzI0Mw==|d20ad0d1e62d84b00a3e74ec201a5917e77b6aef"

      def credentials = new DuoCredentials()
      def request_sig = DuoWeb.signRequest(IKEY, SKEY, AKEY, USER)
      def sigs = request_sig.split(":")
      credentials.signedDuoResponse = RESPONSE + ":" + sigs[1] 

      def configuration = new DuoConfiguration('myHost', IKEY, SKEY, AKEY)
      def c2p = new DuoCredentialsToPrincipalResolver()
      c2p.setDuoConfiguration(configuration)

    when:
      def principal = c2p.resolvePrincipal(credentials)

    then:
      principal.getId() == USER
  }
}