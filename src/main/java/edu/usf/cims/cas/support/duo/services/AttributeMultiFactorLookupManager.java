package edu.usf.cims.cas.support.duo.services;

import edu.ucr.cnc.cas.support.duo.services.ServiceMultiFactorLookupManager;
import net.unicon.cas.addons.serviceregistry.RegisteredServiceWithAttributes;
import org.jasig.cas.services.RegisteredService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * An implementation of {@link ServiceMultiFactorLookupManager} that uses an attribute in the user's principal
 * to decide if Multi-factor Authentication (MFA) is required.
 *
 *
 * For backwards-compatibility, if no value is found, MFA is assumed to NOT be required.
 *
 * @author Eric Pierce
 * @version 1.0
 *
 */
public class AttributeMultiFactorLookupManager implements ServiceMultiFactorLookupManager {

    private String multiFactorRequiredAttributeName = "casMFARequired";
    private String multiFactorRequiredUserListAttributeName = "casMFARequiredUsers";

    private static final String REQUIRE_ALL = "ALL";
    private static final String REQUIRE_CHECK = "CHECK_LIST";
    private static final String REQUIRE_NONE = "NONE";

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonServiceMultiFactorLookupManager.class);

    @Override
    public boolean getMFARequired(RegisteredService registeredService, String username) {

    }
}