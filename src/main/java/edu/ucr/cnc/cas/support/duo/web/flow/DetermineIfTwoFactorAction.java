package edu.ucr.cnc.cas.support.duo.web.flow;

import com.timgroup.statsd.NonBlockingStatsDClient;
import edu.ucr.cnc.cas.support.duo.authentication.principal.UserMultiFactorLookupManager;
import edu.ucr.cnc.cas.support.duo.services.ServiceMultiFactorLookupManager;
import org.apache.log4j.Logger;
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
        final UsernamePasswordCredentials credentials = (UsernamePasswordCredentials) context.getFlowScope().get("credentials");
        final String userId = credentials.getUsername();

        boolean serviceMFARequired = false;
        boolean userMFARequired = false;

        if((this.serviceMultiFactorLookupManager == null) && (this.userMultiFactorLookupManager == null)){
          throw new java.lang.IllegalArgumentException("ServiceMultiFactorLookupManager or UserMultiFactorLookupManager required!");
        }

        // Only perform this if a servicesManager and a ServiceMultiFactorLookupManager are specified in MultiFactorCasConfiguration.xml
        if((this.servicesManager != null) && (this.serviceMultiFactorLookupManager != null)) {
            // Get the registered service from flow scope
            Service service = (Service)context.getFlowScope().get("service");
            RegisteredService registeredService = this.servicesManager.findServiceBy(service);
            serviceMFARequired = this.serviceMultiFactorLookupManager.getMFARequired(registeredService, userId);
        }

        // If the service requires MFA, it's required for all
        if(serviceMFARequired) {
            if(this.logToStatsD) this.statsDClient.incrementCounter(this.MFA_NEEDED);
            this.logger.debug("Multi-factor required by service.  " + userId + " result is " + this.MFA_NEEDED);
            return result(this.MFA_NEEDED);
        }

        // Only perform this if a userMultiFactorLookupManager is specified in MultiFactorCasConfiguration.xml
        if((this.userMultiFactorLookupManager != null)) {
          userMFARequired = this.userMultiFactorLookupManager.getMFARequired(userId);
        }

        // If the user requires it and the service is optional, it is required
        if(userMFARequired) {
            if(this.logToStatsD) this.statsDClient.incrementCounter(this.MFA_NEEDED);
            this.logger.debug("Multi-factor required by user.  " + userId + " result is " + this.MFA_NEEDED);
            return result(this.MFA_NEEDED);
        }

        if(this.logToStatsD) this.statsDClient.incrementCounter(this.NO_MFA_NEEDED);
        this.logger.debug("Multi-factor not required by service or user.  " + userId + " result is " + this.NO_MFA_NEEDED);
        return result(this.NO_MFA_NEEDED);
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