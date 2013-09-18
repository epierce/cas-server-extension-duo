package edu.ucr.cnc.cas.support.duo.authentication.principal

import spock.lang.Specification
import com.github.trevershick.test.ldap.LdapServerResource
import com.github.trevershick.test.ldap.annotations.*
import org.springframework.ldap.core.support.LdapContextSource

//The LDAP data we will use for the tests
@LdapConfiguration(
    bindDn = "uid=admin",
    password = "password",
    port = 11111,
    base = @LdapEntry(dn='dc=example,dc=edu', objectclass=['top','domain']),
    entries = [
      @LdapEntry(
        dn='uid=testUser1,dc=example,dc=edu',
        objectclass=['top','person','inetorgperson'],
        attributes=[
          @LdapAttribute(name="cn",value="Test User1"),
          @LdapAttribute(name="sn",value="User1"),
          @LdapAttribute(name="uid",value="testUser1"),
          //Use employeeType to store the MFA requirement so we don't have to create a custom attribute 
          @LdapAttribute(name="employeeType",value="YES")
        ]
      ),
      @LdapEntry(
        dn='uid=testUser2,dc=example,dc=edu',
        objectclass=['top','person','inetorgperson'],
        attributes=[
          @LdapAttribute(name="cn",value="Test User2"),
          @LdapAttribute(name="sn",value="User2"),
          @LdapAttribute(name="uid",value="testUser2"),
          //Use employeeType to store the MFA requirement so we don't have to create a custom attribute 
          @LdapAttribute(name="employeeType",value="NO")
        ]
      ),
      @LdapEntry(
        dn='uid=testUser3,dc=example,dc=edu',
        objectclass=['top','person','inetorgperson'],
        attributes=[
          @LdapAttribute(name="cn",value="Test User3"),
          @LdapAttribute(name="sn",value="User3"),
          @LdapAttribute(name="uid",value="testUser3")
        ]
      ),
      @LdapEntry(
        dn='uid=testUser4,dc=example,dc=edu',
        objectclass=['top','person','inetorgperson'],
        attributes=[
          @LdapAttribute(name="cn",value="Test User4"),
          @LdapAttribute(name="sn",value="User4"),
          @LdapAttribute(name="uid",value="testUser4"),
          //Use employeeType to store the MFA requirement so we don't have to create a custom attribute 
          //Use a custom value
          @LdapAttribute(name="employeeType",value="Use_MFA")
        ]
      )  
    ]
)
class LdapUserMultiFactorLookupManagerTests extends Specification {

  def server

  def setup() {
    //Create in-memory LDAP server
    server = new LdapServerResource(this).start()
  }

  def cleanup() {
    server.stop()
  }

    def "Login with a user that requires MFA"(){
      given:
        def username = 'testUser1'
        def searchFilter = '(uid=%u)'
        def multiFactorAttributeName = 'employeeType'
        def bindDn = 'uid=admin'
        def bindPassword = 'password'
        def ldapUrl = 'ldap://localhost:11111'
        def searchBase = 'dc=example,dc=edu'

        //Create the Ldap connection
        def source = new LdapContextSource()
        source.userDn = bindDn
        source.password = bindPassword
        source.url = ldapUrl
        source.afterPropertiesSet()

      when:
        def lookupManager = new LdapUserMultiFactorLookupManager()
        lookupManager.contextSource = source
        lookupManager.filter = searchFilter
        lookupManager.searchBase = searchBase
        lookupManager.multiFactorAttributeName = multiFactorAttributeName

        def result = lookupManager.getMFARequired(username)

      then:
        result == true
    }

  def "Login with a user that does not require MFA"(){
      given:
        def username = 'testUser2'
        def searchFilter = '(uid=%u)'
        def multiFactorAttributeName = 'employeeType'
        def bindDn = 'uid=admin'
        def bindPassword = 'password'
        def ldapUrl = 'ldap://localhost:11111'
        def searchBase = 'dc=example,dc=edu'

        //Create the Ldap connection
        def source = new LdapContextSource()
        source.userDn = bindDn
        source.password = bindPassword
        source.url = ldapUrl
        source.afterPropertiesSet()

      when:
        def lookupManager = new LdapUserMultiFactorLookupManager()
        lookupManager.contextSource = source
        lookupManager.filter = searchFilter
        lookupManager.searchBase = searchBase
        lookupManager.multiFactorAttributeName = multiFactorAttributeName

        def result = lookupManager.getMFARequired(username)

      then:
        result == false
    }

  def "Login with a user that does not have a MFA attribute set"(){
      given:
        def username = 'testUser3'
        def searchFilter = '(uid=%u)'
        def multiFactorAttributeName = 'employeeType'
        def bindDn = 'uid=admin'
        def bindPassword = 'password'
        def ldapUrl = 'ldap://localhost:11111'
        def searchBase = 'dc=example,dc=edu'

        //Create the Ldap connection
        def source = new LdapContextSource()
        source.userDn = bindDn
        source.password = bindPassword
        source.url = ldapUrl
        source.afterPropertiesSet()

      when:
        def lookupManager = new LdapUserMultiFactorLookupManager()
        lookupManager.contextSource = source
        lookupManager.filter = searchFilter
        lookupManager.searchBase = searchBase
        lookupManager.multiFactorAttributeName = multiFactorAttributeName

        def result = lookupManager.getMFARequired(username)

      then:
        result == false
    }

    def "Login with a user that requires MFA - alternate value"(){
      given:
        def username = 'testUser4'
        def searchFilter = '(uid=%u)'
        def multiFactorAttributeName = 'employeeType'
        def multiFactorAttributeValue = 'Use_MFA'
        def bindDn = 'uid=admin'
        def bindPassword = 'password'
        def ldapUrl = 'ldap://localhost:11111'
        def searchBase = 'dc=example,dc=edu'

        //Create the Ldap connection
        def source = new LdapContextSource()
        source.userDn = bindDn
        source.password = bindPassword
        source.url = ldapUrl
        source.afterPropertiesSet()

      when:
        def lookupManager = new LdapUserMultiFactorLookupManager()
        lookupManager.contextSource = source
        lookupManager.filter = searchFilter
        lookupManager.searchBase = searchBase
        lookupManager.multiFactorAttributeName = multiFactorAttributeName
        lookupManager.multiFactorAttributeValue = multiFactorAttributeValue

        def result = lookupManager.getMFARequired(username)

      then:
        result == true
    }
}