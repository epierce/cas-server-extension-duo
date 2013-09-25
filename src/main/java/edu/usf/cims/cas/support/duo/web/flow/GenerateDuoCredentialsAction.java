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
        
        // Get the TGT id from the flow scope and retrieve the actual TGT from the ticket registry
        String ticketGrantingTicketId = (String)context.getFlowScope().get("ticketGrantingTicketId");
        TicketGrantingTicket ticketGrantingTicket = (TicketGrantingTicket) this.ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);

        LOGGER.debug("Retrieved authentication context. Building Duo credentials...");
        DuoCredentials credentials = new DuoCredentials();

        credentials.setFirstAuthentication(ticketGrantingTicket.getAuthentication());
        LOGGER.debug("Added first authentication [{}] to the DuoCredential", ticketGrantingTicket.getAuthentication());

        credentials.setFirstCredentials(origCredentials);
        LOGGER.debug("Added first credential [{}] to the DuoCredential", origCredentials);

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