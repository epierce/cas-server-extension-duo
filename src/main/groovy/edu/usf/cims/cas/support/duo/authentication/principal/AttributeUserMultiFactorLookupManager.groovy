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
  def multiFactorAttributeName = "casmfarequired";
  def multiFactorAttributeValue = "yes";

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

    if (userAttrCleaned[this.multiFactorAttributeName] == null) {
      LOGGER.debug("No attribute value for [{}] for user [{}]", this.multiFactorAttributeName, principal.id)
      return false
    } else if (userAttrCleaned[this.multiFactorAttributeName] instanceof Collection){
      return userAttrCleaned[this.multiFactorAttributeName].contains(this.multiFactorAttributeValue)
    } else {
      return userAttrCleaned[this.multiFactorAttributeName].equalsIgnoreCase(this.multiFactorAttributeValue)
    }
  }

  String getMultiFactorAttributeName() {
    return multiFactorAttributeName
  }

  void setMultiFactorAttributeName(String multiFactorAttributeName) {
    this.multiFactorAttributeName = multiFactorAttributeName.toLowerCase()
  }

  String getMultiFactorAttributeValue() {
    return multiFactorAttributeValue
  }

  void setMultiFactorAttributeValue(String multiFactorAttributeValue) {
    this.multiFactorAttributeValue = multiFactorAttributeValue.toLowerCase()
  }
}