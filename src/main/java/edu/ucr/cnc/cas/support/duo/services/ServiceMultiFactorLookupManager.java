package edu.ucr.cnc.cas.support.duo.services;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.authentication.principal.Principal;

/**
 * Object used by Spring action that looks up whether a second factor is required.
 *
 * @author Michael Kennedy
 * @version 1.1
 *
 */
public interface ServiceMultiFactorLookupManager {
    public boolean getMFARequired(RegisteredService service, Principal principal);
}