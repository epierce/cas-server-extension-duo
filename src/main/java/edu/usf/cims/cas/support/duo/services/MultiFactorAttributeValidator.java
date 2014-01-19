package edu.usf.cims.cas.support.duo.services;

import java.util.Map;

/**
 * An validation strategy interface for determining if Duo credentials are required based on user attributes.
 *
 * @author Eric Pierce
 */

public interface MultiFactorAttributeValidator {
    /**
     * Determine if Duo credentials are required by comparing configured registered service authorization attributes
     * with the actual resolved attributes of an authenticated principal.
     *
     * @param serviceAttributes
     * @param authenticatedPrincipalAttributes
     *
     * @return true if authorized, false otherwise
     */
    boolean check(Map serviceAttributes, Map authenticatedPrincipalAttributes);
}