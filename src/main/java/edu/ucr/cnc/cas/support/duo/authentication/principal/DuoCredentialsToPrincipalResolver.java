package edu.ucr.cnc.cas.support.duo.authentication.principal;

import com.duosecurity.DuoWeb;
import org.jasig.cas.authentication.principal.Credentials;
import edu.ucr.cnc.cas.support.duo.DuoConfiguration;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import javax.validation.constraints.NotNull;

/**
 * DuoCredentialsToPrincipalResolver is responsible for converting a {@link DuoCredentials} to
 * a CAS {@link Principal}. This is used by the authentication process after an authentication succeeds.
 *
 * This uses the same Duo web service to verify the Duo signed response and get a username.
 *
 * @author Michael Kennedy <michael.kennedy@ucr.edu>
 * @version 1.0
 */
public class DuoCredentialsToPrincipalResolver implements CredentialsToPrincipalResolver {

    /**
     * This should be injected via Spring in duoConfiguration.xml
     */
    @NotNull
    private DuoConfiguration duoConfiguration;

    /**
     * Returns a {@link Principal} based on a {@link DuoCredentials} credential.
     *
     * @param credentials a DuoCredentials object
     * @return a {@link Principal} based on a {@link DuoCredentials} credential
     */
    @Override
    public Principal resolvePrincipal(Credentials credentials) {
        final DuoCredentials duoCredentials = (DuoCredentials)credentials;

        // Do an out of band request using the DuoWeb api to the hosted duo service, if it is successful
        // it will return a String containing the username of the successfully authenticated user, but will
        // return a blank String otherwise.
        String duoVerifyResponse = DuoWeb.verifyResponse(this.duoConfiguration.getIntegrationKey(),
                this.duoConfiguration.getSecretKey(),
                this.duoConfiguration.getApplicationKey(),
                duoCredentials.getSignedDuoResponse());

        SimplePrincipal simplePrincipal = new SimplePrincipal(duoVerifyResponse);

        return simplePrincipal;
    }

    /**
     * Determines whether a particular credential is supported by this {@link CredentialsToPrincipalResolver}. This
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
}
