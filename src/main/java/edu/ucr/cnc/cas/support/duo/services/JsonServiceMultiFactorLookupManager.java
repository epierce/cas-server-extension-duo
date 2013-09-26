package edu.ucr.cnc.cas.support.duo.services;

import edu.ucr.cnc.cas.support.duo.services.ServiceMultiFactorLookupManager;
import net.unicon.cas.addons.serviceregistry.RegisteredServiceWithAttributes;
import org.jasig.cas.services.RegisteredService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * An implementation of {@link ServiceMultiFactorLookupManager} that uses an attribute in the JSON service
 * registry to decide if Multi-factor Authentication (MFA) is required.  
 *
 * There are three possible values for the attribute:
 *   * 'ALL' - Require all users accessing this service to use MFA
 *   * 'NONE' - Do not require MFA for users accessing this service
 *   * 'CHECK_LIST' - Only users listed in multiFactorRequiredUserList are required to use MFA
 *
 * For backwards-compatibility, if no value is found, MFA is assumed to NOT be required.
 *
 * @author Michael Kennedy
 * @version 1.1
 * @see net.unicon.cas.addons.serviceregistry.JsonServiceRegistryDao
 *
 */
public class JsonServiceMultiFactorLookupManager implements ServiceMultiFactorLookupManager {

    private String multiFactorRequiredAttributeName = "casMFARequired";
    private String multiFactorRequiredUserListAttributeName = "casMFARequiredUsers";

    private static final String REQUIRE_ALL = "ALL";
    private static final String REQUIRE_CHECK = "CHECK_LIST";
    private static final String REQUIRE_NONE = "NONE";

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonServiceMultiFactorLookupManager.class);

    @Override
    public boolean getMFARequired(RegisteredService registeredService, String username) {
        if (registeredService instanceof RegisteredServiceWithAttributes) {
            RegisteredServiceWithAttributes registeredServiceWithAttributes = (RegisteredServiceWithAttributes)registeredService;
            String result = (String) registeredServiceWithAttributes.getExtraAttributes().get(this.multiFactorRequiredAttributeName);
            
            if(result == null){
              LOGGER.debug("No MultiFactor requirement found for service {}", registeredServiceWithAttributes.getServiceId());
              return false;
            }

            LOGGER.debug("Check MultiFactor requirement for service {} returned: {}", registeredServiceWithAttributes.getServiceId(), result);
            if (result.equalsIgnoreCase(REQUIRE_NONE)) {  
                return false;
            } else if (result.equalsIgnoreCase(REQUIRE_ALL)) {
                return true;
            } else if (result.equalsIgnoreCase(REQUIRE_CHECK)) {
                //Compare the username to the list from the service registry
                List mfaUsers = getMFARequiredUsers(registeredService);
                LOGGER.debug("MultiFactor required for service {} and users: {}", registeredServiceWithAttributes.getServiceId(), mfaUsers.toString());
                return mfaUsers.contains(username);
            } else {
                LOGGER.warn("MultiFactor check for service {} returned unhandled results: {}", registeredServiceWithAttributes.getServiceId(), result);
                return false;
            }
        }

        return false;
    }

    public List getMFARequiredUsers(RegisteredService registeredService) {
        if (registeredService instanceof RegisteredServiceWithAttributes) {
            RegisteredServiceWithAttributes registeredServiceWithAttributes = (RegisteredServiceWithAttributes)registeredService;
            return (List) registeredServiceWithAttributes.getExtraAttributes().get(this.multiFactorRequiredUserListAttributeName);
        }

        return null;
    }

    public String getMultiFactorRequiredAttributeName() {
        return multiFactorRequiredAttributeName;
    }

    public void setMultiFactorRequiredAttributeName(String multiFactorRequiredAttributeName) {
        this.multiFactorRequiredAttributeName = multiFactorRequiredAttributeName;
    }

    public String getMultiFactorRequiredUserListAttributeName() {
        return multiFactorRequiredUserListAttributeName;
    }

    public void setMultiFactorRequiredUserListAttributeName(String multiFactorRequiredUserListAttributeName) {
        this.multiFactorRequiredUserListAttributeName = multiFactorRequiredUserListAttributeName;
    }
}