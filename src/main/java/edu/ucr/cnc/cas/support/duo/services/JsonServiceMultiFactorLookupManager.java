package edu.ucr.cnc.cas.support.duo.services;

import net.unicon.cas.addons.serviceregistry.RegisteredServiceWithAttributes;
import org.jasig.cas.services.RegisteredService;

/**
 * An implementation of {@link ServiceMultiFactorLookupManager} that uses attributes in JSON services
 * registry to make the determination.
 *
 * @author Michael Kennedy
 * @version 1.0
 * @see net.unicon.cas.addons.serviceregistry.JsonServiceRegistryDao
 *
 */
public class JsonServiceMultiFactorLookupManager implements ServiceMultiFactorLookupManager {

    private String multiFactorAttributeName;

    @Override
    public String getMFARequiredValue(RegisteredService registeredService) {
        if (registeredService instanceof RegisteredServiceWithAttributes) {
            RegisteredServiceWithAttributes registeredServiceWithAttributes = (RegisteredServiceWithAttributes)registeredService;
            return (String)registeredServiceWithAttributes.getExtraAttributes().get(this.multiFactorAttributeName);
        }

        return null;
    }

    public String getSecondFactorAttributeName() {
        return multiFactorAttributeName;
    }

    public void setSecondFactorAttributeName(String multiFactorAttributeName) {
        this.multiFactorAttributeName = multiFactorAttributeName;
    }
}
