package com.duosecurity

import spock.lang.Specification

class DuoSecurityTests extends Specification {

  def "Test signing a Duo Security request"() {
    given: 'signRequest is called with all parameters'
        /* Dummy IKEY and SKEY values */
        def IKEY = "DIXXXXXXXXXXXXXXXXXX"
        def SKEY = "deadbeefdeadbeefdeadbeefdeadbeefdeadbeef"
        def AKEY = "useacustomerprovidedapplicationsecretkey"

        /* Dummy username */
        def USER = "testuser"

    when: 'A request signature is returned'
      def request_sig = DuoWeb.signRequest(IKEY, SKEY, AKEY, USER)

    then:
      request_sig != null
  }

  def "Test signing a Duo Security request without a username"() {
    given: 'signRequest is called with all parameters except username'
        /* Dummy IKEY and SKEY values */
        def IKEY = "DIXXXXXXXXXXXXXXXXXX"
        def SKEY = "deadbeefdeadbeefdeadbeefdeadbeefdeadbeef"
        def AKEY = "useacustomerprovidedapplicationsecretkey"

        /* Empty username */
        def USER = ""

    when: 'An error is returned'
      def request_sig = DuoWeb.signRequest(IKEY, SKEY, AKEY, USER)

    then:
      request_sig == DuoWeb.ERR_USER
  }

  def "Test signing a Duo Security request with an invalid integration key"() {
    given: 'signRequest is called with an invalid integration key'
        /* Dummy IKEY and SKEY values */
        def IKEY = "invalid"
        def SKEY = "deadbeefdeadbeefdeadbeefdeadbeefdeadbeef"
        def AKEY = "useacustomerprovidedapplicationsecretkey"

        /* Empty username */
        def USER = "testuser"

    when: 'An error is returned'
      def request_sig = DuoWeb.signRequest(IKEY, SKEY, AKEY, USER)

    then:
      request_sig == DuoWeb.ERR_IKEY
  }  

  def "Test signing a Duo Security request with an invalid secret key"() {
    given: 'signRequest is called with an invalid secret key'
        /* Dummy IKEY and SKEY values */
        def IKEY = "DIXXXXXXXXXXXXXXXXXX"
        def SKEY = "invalid"
        def AKEY = "useacustomerprovidedapplicationsecretkey"

        /* Empty username */
        def USER = "testuser"

    when: 'An error is returned'
      def request_sig = DuoWeb.signRequest(IKEY, SKEY, AKEY, USER)

    then:
      request_sig == DuoWeb.ERR_SKEY
  } 

  def "Test signing a Duo Security request with an invalid application key"() {
    given: 'signRequest is called with an invalid application key'
        /* Dummy IKEY and SKEY values */
        def IKEY = "DIXXXXXXXXXXXXXXXXXX"
        def SKEY = "deadbeefdeadbeefdeadbeefdeadbeefdeadbeef"
        def AKEY = "invalid"

        /* Empty username */
        def USER = "testuser"

    when: 'An error is returned'
      def request_sig = DuoWeb.signRequest(IKEY, SKEY, AKEY, USER)

    then:
      request_sig == DuoWeb.ERR_AKEY
  } 

  def "Check the Duo Security request result - success"() {
    given: 'signRequest is called with all parameters'
        /* Dummy IKEY and SKEY values */
        def IKEY = "DIXXXXXXXXXXXXXXXXXX"
        def SKEY = "deadbeefdeadbeefdeadbeefdeadbeefdeadbeef"
        def AKEY = "useacustomerprovidedapplicationsecretkey"

        /* Dummy username */
        def USER = "testuser"

        def FUTURE_RESPONSE = "AUTH|dGVzdHVzZXJ8RElYWFhYWFhYWFhYWFhYWFhYWFh8MTYxNTcyNzI0Mw==|d20ad0d1e62d84b00a3e74ec201a5917e77b6aef"

    when: 'A request signature is returned'
      def request_sig = DuoWeb.signRequest(IKEY, SKEY, AKEY, USER)
      def sigs = request_sig.split(":")
      def valid_app_sig = sigs[1]
      def verifyResponse = DuoWeb.verifyResponse(IKEY, SKEY, AKEY, FUTURE_RESPONSE + ":" + valid_app_sig)

    then:
      verifyResponse == USER
  }

  def "Check the Duo Security request result - invalid application key"() {
    given: 'signRequest is called with all parameters'
        /* Dummy IKEY and SKEY values */
        def IKEY = "DIXXXXXXXXXXXXXXXXXX"
        def SKEY = "deadbeefdeadbeefdeadbeefdeadbeefdeadbeef"
        def AKEY = "invalidinvalidinvalidinvalidinvalidinvalid"

        /* Dummy username */
        def USER = "testuser"

        def FUTURE_RESPONSE = "AUTH|INVALID|SIG"

    when: 'A request signature is returned'
      def request_sig = DuoWeb.signRequest(IKEY, SKEY, AKEY, USER)
      def sigs = request_sig.split(":")
      def valid_app_sig = sigs[1]
      def verifyResponse = DuoWeb.verifyResponse(IKEY, SKEY, AKEY, FUTURE_RESPONSE + ":" + valid_app_sig)

    then:
      verifyResponse == null
  }

  def "Check the Duo Security request result - expired response"() {
    given: 'signRequest is called with all parameters'
        /* Dummy IKEY and SKEY values */
        def IKEY = "DIXXXXXXXXXXXXXXXXXX"
        def SKEY = "deadbeefdeadbeefdeadbeefdeadbeefdeadbeef"
        def AKEY = "useacustomerprovidedapplicationsecretkey"

        /* Dummy username */
        def USER = "testuser"

        def FUTURE_RESPONSE = "AUTH|dGVzdHVzZXJ8RElYWFhYWFhYWFhYWFhYWFhYWFh8MTMwMDE1Nzg3NA==|cb8f4d60ec7c261394cd5ee5a17e46ca7440d702"

    when: 'A request signature is returned'
      def request_sig = DuoWeb.signRequest(IKEY, SKEY, AKEY, USER)
      def sigs = request_sig.split(":")
      def expired_app_sig = sigs[1]
      def verifyResponse = DuoWeb.verifyResponse(IKEY, SKEY, AKEY, FUTURE_RESPONSE + ":" + expired_app_sig)

    then:
      verifyResponse == null
  }
}