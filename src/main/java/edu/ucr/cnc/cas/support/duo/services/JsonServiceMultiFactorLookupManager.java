package edu.ucr.cnc.cas.support.duo.services;

import edu.ucr.cnc.cas.support.duo.services.ServiceMultiFactorLookupManager;
import net.unicon.cas.addons.serviceregistry.RegisteredServiceWithAttributes;
import org.jasig.cas.services.RegisteredService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;

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

    private static final Logger logger = LoggerFactory.getLogger(JsonServiceMultiFactorLookupManager.class);


    @Override
    public boolean getMFARequired(RegisteredService registeredService, String username) {
        if (registeredService instanceof RegisteredServiceWithAttributes) {
            RegisteredServiceWithAttributes registeredServiceWithAttributes = (RegisteredServiceWithAttributes)registeredService;
            String result = (String) registeredServiceWithAttributes.getExtraAttributes().get(this.multiFactorRequiredAttributeName);
            logger.debug("Check MultiFactor Required for Service returned: {}", result);
            if (result.equalsIgnoreCase(REQUIRE_NONE)) {  
                return false;
            } else if (result.equalsIgnoreCase(REQUIRE_ALL)) {
                return true;
            } else if (result.equalsIgnoreCase(REQUIRE_CHECK)) {
                //Compare the username to the list from the service registry
                List mfaUsers = getMFARequiredUsers(registeredService);
                logger.debug("These users must use MFA: {}", mfaUsers.toString());
                return mfaUsers.contains(username.toLowerCase());
            } else {
                logger.error("MultiFactor check returned unknown results: {}", result);
                return false;
            }
        }

        return false;
    }

    public List getMFARequiredUsers(RegisteredService registeredService) {
        if (registeredService instanceof RegisteredServiceWithAttributes) {
            RegisteredServiceWithAttributes registeredServiceWithAttributes = (RegisteredServiceWithAttributes)registeredService;
            try {
              return toList( (JSONArray)registeredServiceWithAttributes.getExtraAttributes().get(this.multiFactorRequiredUserListAttributeName));
            } catch (JSONException e) {
              logger.error("Could not parse value in {}!", this.multiFactorRequiredUserListAttributeName);
              logger.debug(e.toString());
            }
        }

        return null;
    }

    private List toList(JSONArray array) throws JSONException {
        List list = new ArrayList();
        for (int i = 0; i < array.length(); i++) {
          list.add(array.get(i).toString());
        }
        return list;
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