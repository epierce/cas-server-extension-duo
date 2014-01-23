package edu.ucr.cnc.cas.support.duo.authentication.principal;
import org.jasig.cas.authentication.principal.Principal;

/**
 * Object used by Spring action that looks up whether a second factor is required.
 *
 * @author Michael Kennedy
 * @version 1.1
 *
 */
public interface UserMultiFactorLookupManager {
    public boolean getMFARequired(Principal principal);
}
