package edu.ucr.cnc.cas.duo.authentication;

import edu.ucr.cnc.cas.duo.authentication.principal.DuoCredentials;
import edu.ucr.cnc.cas.support.CasConstants;
import org.apache.log4j.Logger;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.MutableAuthentication;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;

/**
 * DuoAuthenticationMetaDataPopulator implements the {@link AuthenticationMetaDataPopulator} interface and is responsible
 * for adding additional an additional attribute to CAS TGT that the authentication was done using a second factor, in this
 * case a Duo authentication.
 *
 * @author Michael Kennedy <michael.kennedy@ucr.edu>
 * @version 1.0
 */
public class DuoAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {

    private Logger logger = Logger.getLogger(getClass());

    /**
     * Returns an {@link Authentication} object with the added LOA_TF attributed appended.
     *
     * @param authentication
     * @param credentials
     * @return
     */
    @Override
    public Authentication populateAttributes(Authentication authentication, Credentials credentials) {

        // Only do anything if the credentials being passed is of type DuoCredentials
        if (credentials instanceof DuoCredentials) {
            Principal simplePrincipal = new SimplePrincipal(authentication.getPrincipal().getId());
            MutableAuthentication mutableAuthentication = new MutableAuthentication(simplePrincipal, authentication.getAuthenticatedDate());

            // Add the LOA_TF attribute to the Authentication object
            mutableAuthentication.getAttributes().putAll(authentication.getAttributes());
            mutableAuthentication.getAttributes().put(CasConstants.LOA_ATTRIBUTE, CasConstants.LOA_TF);

            this.logger.debug("adding LOA of " + CasConstants.LOA_TF + " to Authentication object for " + simplePrincipal.getId());

            return mutableAuthentication;
        }

        return authentication;
    }
}
