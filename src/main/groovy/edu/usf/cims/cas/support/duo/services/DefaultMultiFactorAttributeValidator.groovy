package edu.usf.cims.cas.support.duo.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DefaultMultiFactorAttributeValidator implements MultiFactorAttributeValidator {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMultiFactorAttributeValidator.class)

  @Override
  boolean check(Map serviceAttributes, Map userAttributes){
    serviceAttributes.keySet().intersect(userAttributes.keySet()).any {
      if(serviceAttributes[it] instanceof Collection){
        return serviceAttributes[it].intersect([userAttributes[it]].flatten())
      } else {
        if(userAttributes[it] instanceof Collection){
          return userAttributes[it].intersect([serviceAttributes[it]].flatten())
        } else {
          return serviceAttributes[it].equalsIgnoreCase(userAttributes[it])
        }
      }
    }
  }
}