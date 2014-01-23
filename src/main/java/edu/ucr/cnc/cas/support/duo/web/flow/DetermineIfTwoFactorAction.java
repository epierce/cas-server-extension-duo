package edu.ucr.cnc.cas.support.duo.web.flow;

import net.unicon.cas.addons.authentication.AuthenticationSupport;
import net.unicon.cas.addons.authentication.internal.DefaultAuthenticationSupport;
import edu.ucr.cnc.cas.support.duo.authentication.principal.UserMultiFactorLookupManager;
import edu.ucr.cnc.cas.support.duo.services.ServiceMultiFactorLookupManager;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spring action for determining if a user requires multi-factor authentication
 *
 * @author Michael Kennedy
 * @version 1.1
 *
 */
public class DetermineIfTwoFactorAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DetermineIfTwoFactorAction.class);

    @NotNull
    private TicketRegistry ticketRegistry;

    private ServicesManager servicesManager;
    private AuthenticationSupport authenticationSupport;
    private UserMultiFactorLookupManager userMultiFactorLookupManager;
    private ServiceMultiFactorLookupManager serviceMultiFactorLookupManager;

    final String NO_MFA_NEEDED = "noMultiFactorNeeded";
    final String MFA_NEEDED = "multiFactorNeeded";

    /**
     * Determines whether multi-factor is needed.
     *
     * @param context
     * @return
     * @throws Exception
     */
    @Override
    protected Event doExecute(RequestContext context) throws Exception {

        this.authenticationSupport = new DefaultAuthenticationSupport(ticketRegistry);

        boolean serviceMFARequired = false;
        boolean userMFARequired = false;

        final Principal principal = this.authenticationSupport.getAuthenticatedPrincipalFrom(WebUtils.getTicketGrantingTicketId(context));

        /* Guard against expired SSO sessions. 'error' event should trigger the transition to the 'generateLoginTicket' state */

        if (principal == null) {
            logger.warn("The SSO session is no longer valid. Restarting the login process...");
            return error();
        }

        final Object principalAttributes = principal.getAttributes();
        final String principalId = principal.getId();

        if((this.serviceMultiFactorLookupManager == null) && (this.userMultiFactorLookupManager == null)){
          throw new java.lang.IllegalArgumentException("ServiceMultiFactorLookupManager or UserMultiFactorLookupManager required!");
        }

        /* Only perform this if a servicesManager and a ServiceMultiFactorLookupManager are specified in MultiFactorCasConfiguration.xml */
        if((this.servicesManager != null) && (this.serviceMultiFactorLookupManager != null)) {
            /* Get the registered service from flow scope */
            Service service = (Service)context.getFlowScope().get("service");
            RegisteredService registeredService = this.servicesManager.findServiceBy(service);
            serviceMFARequired = this.serviceMultiFactorLookupManager.getMFARequired(registeredService, principal);
        }

        // If the service requires MFA, it's required for all
        if(serviceMFARequired) {
            LOGGER.debug("Multi-factor required by service.  {} result is {}", principalId, this.MFA_NEEDED);
            return result(this.MFA_NEEDED);
        }

        // Only perform this if a userMultiFactorLookupManager is specified in MultiFactorCasConfiguration.xml
        if((this.userMultiFactorLookupManager != null)) {
          userMFARequired = this.userMultiFactorLookupManager.getMFARequired(principal);
        }

        // If the user requires it and the service is optional, it is required
        if(userMFARequired) {
            LOGGER.debug("Multi-factor required by user.  {} result is {}", principalId, this.MFA_NEEDED);
            return result(this.MFA_NEEDED);
        }

        LOGGER.debug("Multi-factor not required by service or user.  {} result is {}", principalId, this.NO_MFA_NEEDED);
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

    public TicketRegistry getTicketRegistry() {
        return ticketRegistry;
    }

    public void setTicketRegistry(TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }
}