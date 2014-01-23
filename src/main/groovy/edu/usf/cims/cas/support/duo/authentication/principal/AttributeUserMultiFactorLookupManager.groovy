package edu.usf.cims.cas.support.duo.authentication.principal

import edu.ucr.cnc.cas.support.duo.authentication.principal.UserMultiFactorLookupManager
import org.jasig.cas.authentication.principal.Principal

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * An implementation of {@link UserMultiFactorLookupManager} that uses attributes from the user principal to make
 * the determination.  MFA will be required for ALL services.
 *
 * @author Eric Pierce
 * @version 1.0
 */
class AttributeUserMultiFactorLookupManager implements UserMultiFactorLookupManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(AttributeUserMultiFactorLookupManager.class)

  /** The default value to compare to. **/
  def mfaRequiredKey = "casMFARequired".toLowerCase()
  def mfaRequiredValue = "YES".toLowerCase()

  @Override
  boolean getMFARequired(Principal principal) {

    def userAttributes = principal.attributes

    //Convert user attribute map keys/values to all lowercase
    def userAttrCleaned = [:]
    userAttributes.keySet().each {
      if (userAttributes[it] instanceof Collection) {
        userAttrCleaned[it.toLowerCase()] = []
        userAttributes[it].each { val ->
          (userAttrCleaned[it.toLowerCase()]).add(val.toLowerCase())
        }
      } else {
        userAttrCleaned[it.toLowerCase()] = (userAttributes[it] as String).toLowerCase()
      }
    }

    LOGGER.debug("Cleaned up user attributes: {}", userAttrCleaned)

    if (userAttrCleaned[this.mfaRequiredKey] == null) {
      LOGGER.debug("No attribute value for [{}] for user [{}]", this.mfaRequiredKey, principal.id)
      return false
    } else if (userAttrCleaned[this.mfaRequiredKey] instanceof Collection){
      return userAttrCleaned[this.mfaRequiredKey].contains(this.mfaRequiredValue)
    } else {
      return userAttrCleaned[this.mfaRequiredKey].equalsIgnoreCase(this.mfaRequiredValue)
    }
  }

  String getMfaRequiredKey() {
    return mfaRequiredKey
  }

  void setMfaRequiredKey(String mfaRequiredKey) {
    this.mfaRequiredKey = mfaRequiredKey.toLowerCase()
  }

  String getMfaRequiredValue() {
    return mfaRequiredValue
  }

  void setMfaRequiredValue(String mfaRequiredValue) {
    this.mfaRequiredValue = mfaRequiredValue.toLowerCase()
  }
}