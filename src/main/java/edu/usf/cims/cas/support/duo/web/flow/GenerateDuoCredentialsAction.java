package edu.usf.cims.cas.support.duo.web.flow;

import javax.validation.constraints.NotNull;

import edu.usf.cims.cas.support.duo.authentication.principal.DuoCredentials;
import org.apache.commons.lang.StringUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;

public final class GenerateDuoCredentialsAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateDuoCredentialsAction.class);

    public Credentials createDuoCredentials(RequestContext context) {

        UsernamePasswordCredentials origCredentials = (UsernamePasswordCredentials) context.getFlowScope().get("credentials");
        Authentication authentication = (Authentication) context.getFlowScope().get("casAuthentication");

        LOGGER.debug("Retrieved authentication context. Building Duo credentials...");
        DuoCredentials credentials = new DuoCredentials();

        LOGGER.debug("Adding first authentication to the DuoCredential");
        credentials.setFirstAuthentication(authentication);

        LOGGER.debug("Adding first credential to the DuoCredential");
        credentials.setFirstCredentials(origCredentials);

        return credentials;
    }
}