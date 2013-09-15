package edu.ucr.cnc.cas.duo.authentication.handler;

import com.timgroup.statsd.NonBlockingStatsDClient;
import edu.ucr.cnc.cas.duo.config.DuoConfiguration;
import org.apache.log4j.Logger;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.handler.UncategorizedAuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import edu.ucr.cnc.cas.duo.authentication.principal.DuoCredentials;
import com.duosecurity.DuoWeb;
import javax.validation.constraints.NotNull;

/**
 * DuoAuthenticationHandler
 *
 * Bean that implements AuthenticationHandler that can be added to deployerConfigContext.xml to
 * perform authentications on {@link DuoCredentials}.
 *
 * @author  Michael Kennedy <michael.kennedy@ucr.edu>
 * @version 1.1
 *
 */
public class DuoAuthenticationHandler implements AuthenticationHandler {

    /**
     * Injected in duoConfiguration.xml
     */
    private NonBlockingStatsDClient statsDClient;
    private boolean logToStatsD = false;

    /**
     * This should be injected via Spring in duoConfiguration.xml
     */
    @NotNull
    private DuoConfiguration duoConfiguration;

    private Logger logger = Logger.getLogger(getClass());

    /**
     * Returns a boolean indicating whether an authentication using a {@link DuoCredentials} credential
     * was successful.
     *
     * @param credentials a DuoCredentials object
     * @return a boolean indicating whether an authentication was successful
     * @throws AuthenticationException
     */
    @Override
    public boolean authenticate(Credentials credentials) throws AuthenticationException {
        final DuoCredentials duoCredentials = (DuoCredentials)credentials;

        // Do an out of band request using the DuoWeb api to the hosted duo service, if it is successful
        // it will return a String containing the username of the successfully authenticated user, but will
        // return a blank String otherwise.
        String duoVerifyResponse = DuoWeb.verifyResponse(this.duoConfiguration.getIntegrationKey(),
                this.duoConfiguration.getSecretKey(),
                this.duoConfiguration.getApplicationKey(),
                duoCredentials.getSignedDuoResponse());

        if (!duoVerifyResponse.matches("")) {
            this.logger.debug("successful authentication for " + duoVerifyResponse);
            if(this.logToStatsD) this.statsDClient.incrementCounter("duosuccess");
            return true;
        }

        if(this.logToStatsD) this.statsDClient.incrementCounter("duofailure");

        // TODO: Make sure returned netid is the same as the one in the primary auth
        return false;
    }

    /**
     * Determines whether a particular credential is supported by this {@link AuthenticationHandler}. This
     * only supports {@link DuoCredentials}.
     *
     * @param credentials any {@link Credentials} object
     * @return a boolean indicating whether it is supported
     */
    @Override
    public boolean supports(Credentials credentials) {
        return (credentials.getClass() == DuoCredentials.class);
    }

    /**
     * Getter method for duoConfiguration
     *
     * @return a {@link DuoConfiguration} object
     */
    public DuoConfiguration getDuoConfiguration() {
        return duoConfiguration;
    }

    /**
     * Used by Spring injection to set the duoConfiguration object
     *
     * @param duoConfiguration
     */
    public void setDuoConfiguration(DuoConfiguration duoConfiguration) {
        this.duoConfiguration = duoConfiguration;
    }

    public NonBlockingStatsDClient getStatsDClient() {
        return statsDClient;
    }

    public void setStatsDClient(NonBlockingStatsDClient statsDClient) {
        this.statsDClient = statsDClient;
        this.logToStatsD = true;
    }
}