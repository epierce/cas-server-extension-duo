package edu.ucr.cnc.cas.web.flow;

import net.unicon.cas.addons.serviceregistry.RegisteredServiceWithAttributes;
import org.jasig.cas.services.RegisteredService;

/**
 * An implementation of {@link ServiceSecondFactorLookupManager} that uses attributes in JSON services
 * registry to make the determination.
 *
 * @author Michael Kennedy
 * @version 1.0
 * @see net.unicon.cas.addons.serviceregistry.JsonServiceRegistryDao
 *
 */
public class JsonServiceSecondFactorLookupManager implements ServiceSecondFactorLookupManager {

    private String secondFactorAttributeName;

    @Override
    public String getMFARequiredValue(RegisteredService registeredService) {
        if (registeredService instanceof RegisteredServiceWithAttributes) {
            RegisteredServiceWithAttributes registeredServiceWithAttributes = (RegisteredServiceWithAttributes)registeredService;
            return (String)registeredServiceWithAttributes.getExtraAttributes().get(this.secondFactorAttributeName);
        }

        return null;
    }

    public String getSecondFactorAttributeName() {
        return secondFactorAttributeName;
    }

    public void setSecondFactorAttributeName(String secondFactorAttributeName) {
        this.secondFactorAttributeName = secondFactorAttributeName;
    }
}
