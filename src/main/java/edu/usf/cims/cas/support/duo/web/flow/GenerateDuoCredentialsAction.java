package edu.usf.cims.cas.support.duo.web.flow;

import javax.validation.constraints.NotNull;

import edu.usf.cims.cas.support.duo.authentication.principal.DuoCredentials;
import org.apache.commons.lang.StringUtils;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;

public final class GenerateDuoCredentialsAction {

    @NotNull
    private TicketRegistry ticketRegistry;

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateDuoCredentialsAction.class);

    public String createDuoCredentials(RequestContext context) {

        UsernamePasswordCredentials origCredentials = (UsernamePasswordCredentials) context.getFlowScope().get("credentials");

        /* Get the TGT id from the request scope  (this was an inital or re-authentication) */
        String ticketGrantingTicketId = (String)context.getRequestScope().get("ticketGrantingTicketId");
        /* TGT id wasn't in flow scope - look in flow scope (this is an established SSO session) */
        if(ticketGrantingTicketId == null) {
          ticketGrantingTicketId = (String)context.getFlowScope().get("ticketGrantingTicketId");
        }
        LOGGER.debug("Retrieving TGT [{}]", ticketGrantingTicketId);

        /* Now retrieve the actual TGT from the ticket registry */
        TicketGrantingTicket ticketGrantingTicket = (TicketGrantingTicket) this.ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);

        /* If the TGT doesn't exist in the registry or the TGT doesn't contain a useable principal, exit now */
        if(ticketGrantingTicket == null){
          LOGGER.error("Failed to retrieve TGT [{}] from TicketRegistry", ticketGrantingTicketId);
          return "error";
        } else if ( (ticketGrantingTicket.getAuthentication() == null)||
                    (ticketGrantingTicket.getAuthentication().getPrincipal() == null)||
                    (ticketGrantingTicket.getAuthentication().getPrincipal().getId() == null)){
          LOGGER.error("Failed to retrieve authentication principal from TGT [{}]", ticketGrantingTicketId);
          return "error";
        }

        /*
         * If the user has previously authenticated and is accessing a Duo-protected server, the webflow doesn't contain the credential
         * used during the inital login.  Copy the username stored in the TGT into a new UserPasswordCredentials object.
         */
        if((origCredentials == null) || (origCredentials.getUsername() == null)){
            origCredentials = new UsernamePasswordCredentials();
            origCredentials.setUsername(ticketGrantingTicket.getAuthentication().getPrincipal().getId());
        }

        LOGGER.debug("Retrieved authentication context. Building Duo credentials...");
        DuoCredentials credentials = new DuoCredentials();

        credentials.setFirstAuthentication(ticketGrantingTicket.getAuthentication());
        LOGGER.debug("Added first authentication {} to the DuoCredential", ticketGrantingTicket.getAuthentication());

        credentials.setFirstCredentials(origCredentials);
        LOGGER.debug("Added first credential {} to the DuoCredential", origCredentials);

        //Make sure there is a UserPassword credential in Flow Scope (needed for LPPE)
        context.getFlowScope().put("credentials", origCredentials);

        //Add the DuoCredentials into Flow Scope 
        context.getFlowScope().put("duoCredentials", credentials);

        return "created";
    }

    public TicketRegistry getTicketRegistry() {
        return ticketRegistry;
    }

    public void setTicketRegistry(TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }
}
