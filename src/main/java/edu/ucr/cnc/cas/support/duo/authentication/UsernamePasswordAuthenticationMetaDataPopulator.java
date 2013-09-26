package edu.ucr.cnc.cas.support.duo.authentication;

import edu.ucr.cnc.cas.support.duo.CasConstants;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.MutableAuthentication;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UsernamePasswordAuthenticationMetaDataPopulator is used to insert an attribute into a CAS TGT indicating that a
 * single credential was authenticated.
 *
 * @author Michael Kennedy <michael.kennedy@ucr.edu>
 * @version 1.0
 */
public class UsernamePasswordAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator{

    private static final Logger LOGGER = LoggerFactory.getLogger(UsernamePasswordAuthenticationMetaDataPopulator.class);

    /**
     * Returns an {@link Authentication} object with the added LOA_SF attributed appended.
     *
     * @param authentication
     * @param credentials
     * @return
     */
    @Override
    public Authentication populateAttributes(Authentication authentication, Credentials credentials) {

        // Only do anything if the credential being provided is of type UsernamePasswordCredentials
        if (credentials instanceof UsernamePasswordCredentials) {
            Principal simplePrincipal = new SimplePrincipal(authentication.getPrincipal().getId(), authentication.getPrincipal().getAttributes());
            MutableAuthentication mutableAuthentication = new MutableAuthentication(simplePrincipal, authentication.getAuthenticatedDate());

            mutableAuthentication.getAttributes().putAll(authentication.getAttributes());
            mutableAuthentication.getAttributes().put(CasConstants.LOA_ATTRIBUTE, CasConstants.LOA_SF);

            LOGGER.debug("Adding LOA of {} to Authentication object for {}", CasConstants.LOA_SF, simplePrincipal.getId());

            return mutableAuthentication;
        }

        return authentication;
    }
}