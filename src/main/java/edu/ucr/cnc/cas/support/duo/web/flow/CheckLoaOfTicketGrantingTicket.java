package edu.ucr.cnc.cas.support.duo.web.flow;

import edu.ucr.cnc.cas.support.duo.CasConstants;
import edu.ucr.cnc.cas.support.duo.services.ServiceSecondFactorLookupManager;
import org.apache.log4j.Logger;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;

/**
 * CheckLoadOfTicketGrantingTicket is a Spring MVC {@link AbstractAction} that looks at the requirements of
 * the service the user is attempting to login to, as well as the users requirements to use MFA or not and either:
 *
 * 1. Requires a reauthentication using two factors if the original TGT was acquired using a single factor and the
 * service requires two factors.
 * 2. Determines that the LOA of the TGT is sufficient and continues the login webflow.
 *
 * @author Michael Kennedy <michael.kennedy@ucr.edu>
 * @version 1.0
 */
public class CheckLoaOfTicketGrantingTicket extends AbstractAction {

    private Logger logger = Logger.getLogger(getClass());

    @NotNull
    private ServiceSecondFactorLookupManager serviceSecondFactorLookupManager;

    @NotNull
    private ServicesManager servicesManager;

    @NotNull
    private TicketRegistry ticketRegistry;

    @Override
    protected Event doExecute(RequestContext context) throws Exception {
        this.logger.debug("Checking the LOA of a TGT");

        // Get the TGT id from the flow scope and retrieve the actual TGT from the ticket registry
        String ticketGrantingTicketId = (String)context.getFlowScope().get("ticketGrantingTicketId");
        TicketGrantingTicketImpl ticketGrantingTicket;
        ticketGrantingTicket = (TicketGrantingTicketImpl)this.ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);

        // If there isn't a matching TGT in the registry let the user continue
        if (ticketGrantingTicket == null) {
            this.logger.debug("no TGT found for TGT ID '" + ticketGrantingTicketId + "'");
            return result("continue");
        }

        String serviceAuthMechanism = null;

        // Get the registered service from flow scope
        Service service = (Service)context.getFlowScope().get("service");
        RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        serviceAuthMechanism = this.serviceSecondFactorLookupManager.getMFARequiredValue(registeredService);

        // Get the LOA of the current TGT
        String tgtLOA = (String)ticketGrantingTicket.getAuthentication().getAttributes().get(CasConstants.LOA_ATTRIBUTE);

        logger.debug("LOA of TGT " + ticketGrantingTicketId + " is set to " + tgtLOA);

        // Should the user be required to reauthenticate?
        if((serviceAuthMechanism != null) && (!tgtLOA.equals(CasConstants.LOA_TF))) {
            return result("renewForTwoFactor");
        }

        return result("continue");
    }

    public ServiceSecondFactorLookupManager getServiceSecondFactorLookupManager() {
        return serviceSecondFactorLookupManager;
    }

    /**
     * Sets the object that should lookup the second factor mechanism used
     *
     * @param serviceSecondFactorLookupManager a ServiceSecondFactorLookupManager object
     */
    public void setServiceSecondFactorLookupManager(ServiceSecondFactorLookupManager serviceSecondFactorLookupManager) {
        this.serviceSecondFactorLookupManager = serviceSecondFactorLookupManager;
    }

    public ServicesManager getServicesManager() {
        return servicesManager;
    }

    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public TicketRegistry getTicketRegistry() {
        return ticketRegistry;
    }

    public void setTicketRegistry(TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }
}
