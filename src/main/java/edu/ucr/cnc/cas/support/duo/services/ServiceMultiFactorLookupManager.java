package edu.ucr.cnc.cas.support.duo.services;

import org.jasig.cas.services.RegisteredService;

/**
 * Object used by Spring action that looks up whether a second factor is required.
 *
 * @author Michael Kennedy
 * @version 1.1
 *
 */
public interface ServiceMultiFactorLookupManager {
    public boolean getMFARequired(RegisteredService service, String username);
}