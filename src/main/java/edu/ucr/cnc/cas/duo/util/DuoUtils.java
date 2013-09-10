package edu.ucr.cnc.cas.duo.util;

import com.duosecurity.DuoWeb;
import edu.ucr.cnc.cas.duo.config.DuoConfiguration;
import org.apache.log4j.Logger;

/**
 * Performs important tasks related to the Duo authentication process.
 */
public class DuoUtils {

    /**
     * Injected via Spring in duoConfiguration.xml
     */
    private DuoConfiguration duoConfiguration;

    private Logger logger = Logger.getLogger(getClass());

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

        this.logger.debug("generating signing request from Duo for " + username);

        // Use the DuoWeb API to get a Duo request
        String signedRequest = DuoWeb.signRequest(duoConfiguration.getIntegrationKey(),
                duoConfiguration.getSecretKey(), duoConfiguration.getApplicationKey(), username);

        this.logger.debug("Duo returned signed request " + signedRequest);

        return signedRequest;
    }

    public DuoConfiguration getDuoConfiguration() {
        return duoConfiguration;
    }

    public void setDuoConfiguration(DuoConfiguration duoConfiguration) {
        this.duoConfiguration = duoConfiguration;
    }
}
