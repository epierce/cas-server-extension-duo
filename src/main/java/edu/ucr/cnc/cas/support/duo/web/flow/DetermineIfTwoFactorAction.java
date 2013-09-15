package edu.ucr.cnc.cas.support.duo.web.flow;

import com.timgroup.statsd.NonBlockingStatsDClient;
import edu.ucr.cnc.cas.support.duo.authentication.principal.UserMultiFactorLookupManager;
import edu.ucr.cnc.cas.support.duo.services.ServiceMultiFactorLookupManager;
import org.apache.log4j.Logger;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;

/**
 * Spring action for determining if a user requires multi-factor authentication
 *
 * @author Michael Kennedy
 * @version 1.1
 *
 */
public class DetermineIfTwoFactorAction extends AbstractAction {

    final String NO_MFA_NEEDED = "noMultiFactorNeeded";
    final String MFA_NEEDED = "multiFactorNeeded";

    @NotNull
    private String primaryAuthenticationCredentialsName;

    @NotNull
    private CredentialsToPrincipalResolver credentialsToPrincipalResolver;

    @NotNull
    private UserMultiFactorLookupManager userMultiFactorLookupManager;

    private ServiceMultiFactorLookupManager serviceMultiFactorLookupManager;
    private ServicesManager servicesManager;

    private Logger logger = Logger.getLogger(getClass());

    /**
     * Injected in duoConfiguration.xml
     */
    private NonBlockingStatsDClient statsDClient;
    private boolean logToStatsD = false;

    /**
     * Determines whether multi-factor is needed. The wiki has more information at
     * <a href="https://wiki.ucr.edu/display/CIS/CAS+Multifactor+Required+Matrix>https://wiki.ucr.edu/display/CIS/CAS+Multifactor+Required+Matrix</a> for the rules.
     *
     * @param context
     * @return
     * @throws Exception
     * @see <a href="https://wiki.ucr.edu/display/CIS/CAS+Multifactor+Required+Matrix>https://wiki.ucr.edu/display/CIS/CAS+Multifactor+Required+Matrix</a>
     */
    @Override
    protected Event doExecute(RequestContext context) throws Exception {

        // Get the primary authentication credentials which should be of type UsernamePasswordCredentials
        UsernamePasswordCredentials credentials = (UsernamePasswordCredentials)context.getFlowScope().get(this.primaryAuthenticationCredentialsName);
        Principal principal = this.credentialsToPrincipalResolver.resolvePrincipal(credentials);

        String serviceMFARequiredValue = null;

        // Since it isn't required to require services to assert if they require multi-factor auth, only perform this
        // if a servicesManager and a ServiceMultiFactorLookupManager are specified in twoFactorCasConfiguration.xml
        if((this.servicesManager != null) && (this.serviceMultiFactorLookupManager != null)) {
            // Get the registered service from flow scope
            Service service = (Service)context.getFlowScope().get("service");
            RegisteredService registeredService = this.servicesManager.findServiceBy(service);
            serviceMFARequiredValue = this.serviceMultiFactorLookupManager.getMFARequiredValue(registeredService);
        }

        // Get whether the user requires MFA
        String userMFARequiredValue = this.userMultiFactorLookupManager.getMFARequiredValue(principal);

        this.logger.debug(credentials.getUsername() + ": userMFARequiredValue = " + userMFARequiredValue + ", serviceMFARequiredValue = " + serviceMFARequiredValue);

        if(serviceMFARequiredValue == null) {
            if(this.logToStatsD)this.statsDClient.incrementCounter(this.NO_MFA_NEEDED);
            this.logger.debug(credentials.getUsername() + " result is " + this.NO_MFA_NEEDED);
            return result(this.NO_MFA_NEEDED);
        }

        // If the service requires MFA, it's required for all
        if(serviceMFARequiredValue.equals("YES")) {
            if(this.logToStatsD)this.statsDClient.incrementCounter(this.MFA_NEEDED);
            this.logger.debug(credentials.getUsername() + " result is " + this.MFA_NEEDED);
            return result(this.MFA_NEEDED);
        }

        // If the user requires it and the service is optional, it is required
        if(userMFARequiredValue != null && userMFARequiredValue.equals("YES")) {
            if(this.logToStatsD)this.statsDClient.incrementCounter(this.MFA_NEEDED);
            this.logger.debug(credentials.getUsername() + " result is " + this.MFA_NEEDED);
            return result(this.MFA_NEEDED);
        }

        if(this.logToStatsD)this.statsDClient.incrementCounter(this.NO_MFA_NEEDED);
        this.logger.debug(credentials.getUsername() + " result is " + this.NO_MFA_NEEDED);
        return result(this.NO_MFA_NEEDED);
    }

    public String getPrimaryAuthenticationCredentialsName() {
        return primaryAuthenticationCredentialsName;
    }

    public void setPrimaryAuthenticationCredentialsName(String primaryAuthenticationCredentialsName) {
        this.primaryAuthenticationCredentialsName = primaryAuthenticationCredentialsName;
    }

    public UserMultiFactorLookupManager getUserMultiFactorLookupManager() {
        return userMultiFactorLookupManager;
    }

    public void setUserMultiFactorLookupManager(UserMultiFactorLookupManager userMultiFactorLookupManager) {
        this.userMultiFactorLookupManager = userMultiFactorLookupManager;
    }

    public ServicesManager getServicesManager() {
        return servicesManager;
    }

    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public CredentialsToPrincipalResolver getCredentialsToPrincipalResolver() {
        return credentialsToPrincipalResolver;
    }

    public void setCredentialsToPrincipalResolver(CredentialsToPrincipalResolver credentialsToPrincipalResolver) {
        this.credentialsToPrincipalResolver = credentialsToPrincipalResolver;
    }

    public ServiceMultiFactorLookupManager getServiceMultiFactorLookupManager() {
        return serviceMultiFactorLookupManager;
    }

    public void setServiceMultiFactorLookupManager(ServiceMultiFactorLookupManager serviceMultiFactorLookupManager) {
        this.serviceMultiFactorLookupManager = serviceMultiFactorLookupManager;
    }

    public NonBlockingStatsDClient getStatsDClient() {
        return statsDClient;
    }

    public void setStatsDClient(NonBlockingStatsDClient statsDClient) {
        this.logToStatsD = true;
        this.statsDClient = statsDClient;
    }
}