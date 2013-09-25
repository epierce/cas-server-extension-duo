package edu.usf.cims.cas.support.duo.authentication.handler

import spock.lang.Specification
import com.duosecurity.*
import edu.ucr.cnc.cas.support.duo.DuoConfiguration
import org.jasig.cas.authentication.principal.Principal
import org.jasig.cas.authentication.Authentication
import edu.usf.cims.cas.support.duo.authentication.principal.DuoCredentials
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials
import edu.usf.cims.cas.support.duo.authentication.handler.DuoAuthenticationHandler
import edu.usf.cims.cas.support.duo.authentication.principal.DuoCredentialsToPrincipalResolver
import edu.ucr.cnc.cas.support.duo.CasConstants


class DuoAuthenticationHandlerTests extends Specification {

  /**
  * Mock objects that will be used in all tests
  */
  def principal = Mock(Principal)
  def authentication = Mock(Authentication)

  def IKEY = "DIXXXXXXXXXXXXXXXXXX"
  def SKEY = "deadbeefdeadbeefdeadbeefdeadbeefdeadbeef"
  def AKEY = "useacustomerprovidedapplicationsecretkey"
  def USER = "testuser"
  def RESPONSE = "AUTH|dGVzdHVzZXJ8RElYWFhYWFhYWFhYWFhYWFhYWFh8MTYxNTcyNzI0Mw==|d20ad0d1e62d84b00a3e74ec201a5917e77b6aef"

  def "Successful Authentication"(){
    given:
      authentication.principal >> principal
      principal.id  >> USER

      def credentials = new UsernamePasswordCredentials()
      credentials.username = USER
      
      def duoCredentials = new DuoCredentials()
      duoCredentials.setFirstAuthentication(authentication)

      def request_sig = DuoWeb.signRequest(IKEY, SKEY, AKEY, USER)
      def sigs = request_sig.split(":")
      duoCredentials.signedDuoResponse = RESPONSE + ":" + sigs[1] 

      def configuration = new DuoConfiguration('myHost', IKEY, SKEY, AKEY)
      def ah = new DuoAuthenticationHandler()
      ah.setDuoConfiguration(configuration)

    when:
      def authResult = ah.authenticate(duoCredentials)

    then:
      authResult == true
  }

  def "Failed Authentication"(){
    given:
      def BAD_RESPONSE = "AUTH|dGVzdHVzZXJ8RElYWFhYWFhYWFhYWFhYWFhYWFh8MTMwMDE1Nzg3NA==|cb8f4d60ec7c261394cd5ee5a17e46ca7440d702"

      authentication.principal >> principal
      principal.id  >> USER

      def credentials = new UsernamePasswordCredentials()
      credentials.username = USER
      
      def duoCredentials = new DuoCredentials()
      duoCredentials.setFirstAuthentication(authentication)

      def request_sig = DuoWeb.signRequest(IKEY, SKEY, AKEY, USER)
      def sigs = request_sig.split(":")
      duoCredentials.signedDuoResponse = BAD_RESPONSE + ":" + sigs[1] 

      def configuration = new DuoConfiguration('myHost', IKEY, SKEY, AKEY)
      def ah = new DuoAuthenticationHandler()
      ah.setDuoConfiguration(configuration)

    when:
      def authResult = ah.authenticate(duoCredentials)

    then:
      authResult == false
  }

  def "Failed Authentication - username mismatch"(){
    given:
      authentication.principal >> principal
      principal.id  >> "other_user"

      def credentials = new UsernamePasswordCredentials()
      credentials.username = USER
      
      def duoCredentials = new DuoCredentials()
      duoCredentials.setFirstAuthentication(authentication)

      def request_sig = DuoWeb.signRequest(IKEY, SKEY, AKEY, USER)
      def sigs = request_sig.split(":")
      duoCredentials.signedDuoResponse = RESPONSE + ":" + sigs[1] 

      def configuration = new DuoConfiguration('myHost', IKEY, SKEY, AKEY)
      def ah = new DuoAuthenticationHandler()
      ah.setDuoConfiguration(configuration)

    when:
      def authResult = ah.authenticate(duoCredentials)

    then:
      authResult == false
  }
}