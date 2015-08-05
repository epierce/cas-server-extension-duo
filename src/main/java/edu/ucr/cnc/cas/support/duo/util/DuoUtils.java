package edu.ucr.cnc.cas.support.duo.util;

import com.duosecurity.DuoWeb;
import edu.ucr.cnc.cas.support.duo.DuoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs important tasks related to the Duo authentication process.
 */
public class DuoUtils {

    /**
     * Injected via Spring in duoConfiguration.xml
     */
    private DuoConfiguration duoConfiguration;

    private static final Logger LOGGER = LoggerFactory.getLogger(DuoUtils.class);

    /**
     * Generates the signed request via the {@link DuoWeb} API that is embedded in the duo login page so that the javascript authentication process
     * is authenticated.
     *
     * @param username the username in Duo that needs to be authenticated, this username comes from the primary CAS authentication
     *                 via LDAP.
     * @return a String containing a hash that is passed in the javascript code
     * @see <a href="https://www.duosecurity.com/docs/duoweb">Duo Web Developer Documentation</a>
     */
    public String generateSignedRequest(String username) {

        LOGGER.debug("Generating signing request from Duo for {}", username);

        // Use the DuoWeb API to get a Duo request
        String signedRequest = DuoWeb.signRequest(duoConfiguration.getIntegrationKey(),
                duoConfiguration.getSecretKey(), duoConfiguration.getApplicationKey(), username.trim());

        LOGGER.debug("Duo returned signed request {}", signedRequest);

        return signedRequest;
    }

    public DuoConfiguration getDuoConfiguration() {
        return duoConfiguration;
    }

    public void setDuoConfiguration(DuoConfiguration duoConfiguration) {
        this.duoConfiguration = duoConfiguration;
    }
}
