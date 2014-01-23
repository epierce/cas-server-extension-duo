package edu.usf.cims.cas.support.duo.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DefaultMultiFactorAttributeValidator implements MultiFactorAttributeValidator {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMultiFactorAttributeValidator.class)

  @Override
  boolean check(Map serviceAttributes, Map userAttributes){
    //Convert service attribute map keys/values to all lowercase
    def serviceAttrCleaned = [:]
    serviceAttributes.keySet().each {
      if (serviceAttributes[it] instanceof Collection) {
        serviceAttrCleaned[it.toLowerCase()] = []
        serviceAttributes[it].each { val ->
          (serviceAttrCleaned[it.toLowerCase()]).add(val.toLowerCase())
        }
      } else {
        serviceAttrCleaned[it.toLowerCase()] = (serviceAttributes[it] as String).toLowerCase()
      }
    }

    LOGGER.debug("Cleaned up service attributes: {}", serviceAttrCleaned)

    //Now do the same for the user attributes
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

    serviceAttrCleaned.keySet().intersect(userAttrCleaned.keySet()).any {
      if(serviceAttrCleaned[it] instanceof Collection){
        return serviceAttrCleaned[it].intersect([userAttrCleaned[it]].flatten())
      } else {
        if(userAttrCleaned[it] instanceof Collection){
          return userAttrCleaned[it].intersect([serviceAttrCleaned[it]].flatten())
        } else {
          return serviceAttrCleaned[it].equalsIgnoreCase(userAttrCleaned[it])
        }
      }
    }
  }
}