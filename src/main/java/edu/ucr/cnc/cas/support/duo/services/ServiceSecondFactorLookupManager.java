package edu.ucr.cnc.cas.support.duo.services;

import org.jasig.cas.services.RegisteredService;

/**
 * Object used by Spring action that looks up whether a second factor is required.
 *
 * @author Michael Kennedy
 * @version 1.0
 *
 */
public interface ServiceSecondFactorLookupManager {
    public String getMFARequiredValue(RegisteredService service);
}
