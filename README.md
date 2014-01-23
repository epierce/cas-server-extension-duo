cas-server-extension-duo
========================

This module is based on https://github.com/highlnd/cas-overlay-duo  The goal is to extract the code/configuration required to use Duo for two-factor authentication and package it into a module that can be easily included in a CAS deployment.

[DuoSecurity](https://www.duosecurity.com/) provides a hosted service for two-factor authentication using mobile devices, landline phones and hardware tokens.  They provide clients for various applications (VPN, SSH, etc) but an integration for CAS wasn't available.  [Mike Kennedy](https://github.com/highlnd) developed the integration using the [Java DuoWeb Client](https://github.com/duosecurity/duo_java).

Installation & Configuration
===============

### Create a DuoSecurity Account
Signup for a free account on Duo's website: http://duosecurity.com and follow the instructions for creating a new [Duo Web Integration](https://www.duosecurity.com/docs/getting_started).  You'll also need to take note of your:

* Integration Key
* Secret Key
* API Hostname

### Use the Maven Overlay Method for configuring CAS
The wiki article on how to configure it is [here](https://wiki.jasig.org/display/CASUM/Best+Practice+-+Setting+Up+CAS+Locally+using+the+Maven2+WAR+Overlay+Method)

### Use the JSON Service Registry
You'll need to include the [Unicon cas-addons module](https://github.com/Unicon/cas-addons/tree/v1.10) in your Maven overlay.  In particular, you must use the [JSON Service Registry](https://github.com/Unicon/cas-addons/wiki/Configuring%20JSON%20Service%20Registry) to add Duo authentication.  The `extraAttributes` stored with each service are used to determine which services and users require two-factor authentication.

I'll also take a minute to plug one of my other projects: [cas-json-tool](https://github.com/epierce/cas-json-tool)  It's a command-line program for managing the JSON file and includes options for creating and maintaining the service attributes necessary to use Duo authentication.

### Clone the `cas-server-extension-duo` project
```
git clone https://github.com/epierce/cas-server-extension-duo.git
```

### Build the server extension
```
cd cas-server-extension-duo
mvn clean package install
```

### Add the Maven dependency
Add the following block to the `pom.xml` in your CAS overlay

```
<dependency>
  <groupId>edu.usf.cims</groupId>
  <artifactId>cas-server-extension-duo</artifactId>
  <version>0.2.0</version>
</dependency>
```

### Configure Authentication
First, add the `DuoAuthenticationHandler` bean to the list of authentication handlers in `deployerConfigContext.xml`:

```
<property name="authenticationHandlers">
  <list>
   ___other AuthenticationHandlers___
    <bean class="edu.usf.cims.cas.support.duo.authentication.handler.DuoAuthenticationHandler"
      p:duoConfiguration-ref="duoConfiguration" />
  </list>
 </property>
```

* **duoConfiguration-ref**: A reference to the bean that hold the configuration information for Duo (you'll create this later).

You'll also need to add `DuoCredentialsToPrincipalResolver` to the list of principal resolvers:

```
<property name="credentialsToPrincipalResolvers">
  <list>
  ___other credentialToPrincipalResolvers___
    <bean class="edu.usf.cims.cas.support.duo.authentication.principal.DuoCredentialsToPrincipalResolver" />
  </list>
</property>
```

### Configure Authentication Metadata Population
In order to determine if the user's current authentication is sufficient to access a new service (has he logged in with duo or not), we need to add some information onto the user's `Authentication` object.

```
    <property name="authenticationMetaDataPopulators">
      <list>
        <bean class="edu.ucr.cnc.cas.support.duo.authentication.UsernamePasswordAuthenticationMetaDataPopulator"/>
        <bean class="edu.ucr.cnc.cas.support.duo.authentication.DuoAuthenticationMetaDataPopulator"/>
      </list>
    </property>
```
___

### twoFactorCasConfiguration.xml

There are two new files in `WEB-INF/spring-configuration` that need to be configured for your environment.  The first is `twoFactorCasConfiguration.xml`.  Here is an example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:util="http://www.springframework.org/schema/util"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.1.xsd
       http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-3.0.xsd">

    <bean id="serviceLookupManager"
        class="edu.usf.cims.cas.support.duo.services.RegisteredServiceMultiFactorLookupManager">
        <property name="mfaRequiredKey" value="RequireTwoFactor"/>
        <property name="mfaRequiredAttributesKey" value="TwoFactorAttributes"/>
    </bean>

    <bean id="userLookupManager"
        class="edu.usf.cims.cas.support.duo.authentication.principal.AttributeUserMultiFactorLookupManager">
        <property name="mfaRequiredKey" value="RequireTwoFactorForAllServices"/>
        <property name="mfaRequiredValue" value="YES"/>
    </bean>

    <bean id="determineIfTwoFactorAction" class="edu.ucr.cnc.cas.support.duo.web.flow.DetermineIfTwoFactorAction">
        <property name="ticketRegistry" ref="ticketRegistry"/>
        <property name="servicesManager" ref="servicesManager"/>
        <property name="serviceMultiFactorLookupManager" ref="serviceLookupManager"/>
        <property name="userMultiFactorLookupManager" ref="userLookupManager"/>
    </bean>

    <bean id="checkLoaOfTicketGrantingTicket" class="edu.ucr.cnc.cas.support.duo.web.flow.CheckLoaOfTicketGrantingTicket">
        <property name="serviceMultiFactorLookupManager" ref="serviceLookupManager"/>
        <property name="servicesManager" ref="servicesManager"/>
        <property name="ticketRegistry" ref="ticketRegistry"/>
    </bean>

    <bean id="generateDuoCredentials" class="edu.usf.cims.cas.support.duo.web.flow.GenerateDuoCredentialsAction">
        <property name="ticketRegistry" ref="ticketRegistry"/>
    </bean>

</beans>
```

####serviceLookupManager
`RegisteredServiceMultiFactorLookupManager` checks the JSON service registry for the requested service and determines if two-factor authentication is required for the current user.  This bean has two properties:

* `mfaRequiredKey` - Attribute that contains what user population (**ALL**, **NONE**, or **CHECK_ATTRIBUTE**) will be required to use two-factor auth. (default: casMFARequired)
* `mfaRequiredAttributesKey` - This attribute is used when the service's `casMFARequired` attribute equals **CHECK_ATTRIBUTE**.  It contains one or more attributes/values that are compared to the user's attributes to determine if two-factor authentication is required.   (default: casMFAUserAttributes)

Example service entry requiring any user who is a member of **EITHER** the `admins` or `power-users` groups to use Duo authentication.  All other users could login with just username/password credentials.

```JSON
{
    "services": [
        {
            "enabled": true,
            "ignoreAttributes": false,
            "theme": "default",
            "id": 1,
            "extraAttributes": {
                "casMFARequired": "CHECK_ATTRIBUTE",
                "casMFAUserAttributes": {
                  "memberOf": ["admins", "power-users"]
                }
            },
            "allowedToProxy": true,
            "serviceId": "https://example.edu/my_secure_service",
            "description": "Secure service - Admins and power-users need two-factor auth to login",
            "name": "My example service",
            "ssoEnabled": true,
            "anonymousAccess": false,
            "evaluationOrder": 0,
            "allowedAttributes": [

            ]
        }
     ]
}
```

> **NOTE:** `RegisteredServiceMultiFactorLookupManager` compares the attributes/values with the map of attributes from the user's CAS principal, so any user attribute you want to check values for before requiring Duo auth must be configured in the `attributeRepository` bean in `deployerConfigContext.xml`  Releasing the attribute to services is not required, however.

####userLookupManager
The `AttributeUserMultiFactorLookupManager` looks for a specific attribute/value pair in the user's principal to determine is they are required to use Duo authentication for **all** CAS services.

* `mfaRequiredKey` - attribute to return from search (default: casMFARequired)
* `mfaRequiredValue` - The value that, if found, will require the user to authenticate with two-factor for all services.  (default: YES)

####determineIfTwoFactorAction
`DetermineIfTwoFactorAction` is a Spring Webflow action that determines if two-factor authentication is required for the current login webflow.

* `servicesManager` - Reference to the bean configuring the JSON service registry (this value will usually be "servicesManager").
*  `ticketRegistry` - Reference to the bean configuring the server's Ticket Registry (This value will usually be "ticketRegistry").

* `serviceMultiFactorLookupManager` - Reference to the bean (above) that configures `RegisteredServiceMultiFactorLookupManager`
* `userMultiFactorLookupManager` - Reference to the bean (above) that configures `AttributeUserMultiFactorLookupManager`

If you don't want to use one of these lookups, just comment out the bean definition and the `serviceMultiFactorLookupManager` or `userMultiFactorLookupManager` line.

####checkLoaOfTicketGrantingTicket
`CheckLoaOfTicketGrantingTicket` is a Spring Webflow action that determines if the current authentication has a _Level of Assurance_ high enough to access the requested service without requiring re-authentication.
* `serviceMultiFactorLookupManager` - Reference to the bean (above) that configures `JsonServiceMultiFactorLookupManager`
* `servicesManager` - Reference to the bean configuring the JSON service registry (this value will usually be "servicesManager")
* `ticketRegistry` - Reference to the bean configuring the server's Ticket Registry (This value will usually be "ticketRegistry")

####generateDuoCredentials
`GenerateDuoCredentialsAction` is a Spring webflow action that extracts the current authentication object and credentials and uses them to create a new DuoCredential.
* `ticketRegistry` - Reference to the bean configuring the server's Ticket Registry (This value will usaully be "ticketRegistry")

___

### duoConfiguration.xml
This file in `WEB-INF/spring-configuration` configures the use of the DuoWeb Java client.

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:util="http://www.springframework.org/schema/util"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.1.xsd
       http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-3.0.xsd">

    <bean id="duoConfiguration" class="edu.ucr.cnc.cas.support.duo.DuoConfiguration">
        <constructor-arg index="0" value="${duo.apiHost}"/>
        <constructor-arg index="1" value="${duo.integrationKey}"/>
        <constructor-arg index="2" value="${duo.secretKey}"/>
        <constructor-arg index="3" value="${duo.applicationKey}"/>
    </bean>

    <bean id="duoUtils" class="edu.ucr.cnc.cas.support.duo.util.DuoUtils">
        <property name="duoConfiguration" ref="duoConfiguration"/>
    </bean>
</beans>
```

To keep confidential information out of the XML config files, the actual conifuration data should be saved in your `cas.properties` file:

```
duo.apiHost = api-######.duosecurity.com
duo.integrationKey = ABCDEFGHIJKLMNOPQRSTUVWYZ
duo.secretKey = abcdef123456789012abcdef1234567890
duo.applicationKey = reallylongkeygoeshere
```
___

### Update Views
To authenticate with Duo, you'll need to add one new view to your CAS configuration: `casDuoLoginView`

First, add the following lines to the end of properties file for your theme(s) or `default_views.properties` if you are using the default CAS theme.

```
### Duo Login view
casDuoLoginView.(class)=org.springframework.web.servlet.view.JstlView
casDuoLoginView.url=/WEB-INF/view/jsp/default/ui/casDuoLoginView.jsp
```

The JSP page is very simple - all of the user's interaction with the Duo service happens within an iFrame:

```jsp
<jsp:directive.include file="includes/top.jsp" />
    <script src="<c:url value='js/duo/Duo-Web-v1.bundled.min.js'/>"></script>
    <script>
      Duo.init({
          'host': '${apiHost}',
          'sig_request': '${sigRequest}',
          'post_argument': 'signedDuoResponse'
      });
    </script>

    <form:form method="post" id="duo_form" cssClass="fm-v clearfix" commandName="${commandName}" htmlEscape="true">
        <input type="hidden" name="lt" value="${loginTicket}" />
        <input type="hidden" name="execution" value="${flowExecutionKey}" />
        <input type="hidden" name="_eventId" value="submit" />

        <div class="box fl-panel" id="login">
            <iframe id="duo_iframe" width="100%" height="360" frameborder="0"></iframe>
        </div>
    </form:form>

<jsp:directive.include file="includes/bottom.jsp" />
```
___
### login-webflow.xml
The final step is to modify the login webflow to include the checks for TwoFactor requirements and the display of the Duo login view.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<flow xmlns="http://www.springframework.org/schema/webflow"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://www.springframework.org/schema/webflow
                          http://www.springframework.org/schema/webflow/spring-webflow-2.0.xsd">

    <var name="credentials" class="org.jasig.cas.authentication.principal.UsernamePasswordCredentials" />
    <on-start>
        <evaluate expression="initialFlowSetupAction" />
    </on-start>

  <decision-state id="ticketGrantingTicketExistsCheck">
    <if test="flowScope.ticketGrantingTicketId != null" then="hasServiceCheck" else="gatewayRequestCheck" />
  </decision-state>

  <decision-state id="gatewayRequestCheck">
    <if test="requestParameters.gateway != '' and requestParameters.gateway != null and flowScope.service != null" then="gatewayServicesManagementCheck" else="serviceAuthorizationCheck" />
  </decision-state>

  <decision-state id="hasServiceCheck">
    <if test="flowScope.service != null" then="renewRequestCheck" else="viewGenericLoginSuccess" />
  </decision-state>

<!-- changed for duo support
  <decision-state id="renewRequestCheck">
    <if test="requestParameters.renew != '' and requestParameters.renew != null" then="generateLoginTicket" else="generateServiceTicket" />
  </decision-state>
-->
  <decision-state id="renewRequestCheck">
    <if test="requestParameters.renew != '' and requestParameters.renew != null" then="generateLoginTicket" else="checkLoaOfTGT" />
  </decision-state>

<!-- added for Duo Support -->
  <action-state id="checkLoaOfTGT">
    <evaluate expression="checkLoaOfTicketGrantingTicket"/>
    <transition on="continue" to="generateServiceTicket"/>
    <transition on="renewForTwoFactor" to="startDuoSecondFactorFlow"/>
  </action-state>

    <!-- Do a service authorization check early without the need to login first -->
    <action-state id="serviceAuthorizationCheck">
        <evaluate expression="serviceAuthorizationCheck"/>
        <transition to="generateLoginTicket"/>
    </action-state>
<!-- -->

  <!--
    The "warn" action makes the determination of whether to redirect directly to the requested
    service or display the "confirmation" page to go back to the server.
  -->
  <decision-state id="warn">
    <if test="flowScope.warnCookieValue" then="showWarningView" else="redirect" />
  </decision-state>

  <!--
  <action-state id="startAuthenticate">
    <action bean="x509Check" />
    <transition on="success" to="sendTicketGrantingTicket" />
    <transition on="warn" to="warn" />
    <transition on="error" to="generateLoginTicket" />
  </action-state>
   -->

    <!--
      LPPE transitions begin here: You will also need to
      move over the 'lppe-configuration.xml' file from the
      'unused-spring-configuration' folder to the 'spring-configuration' folder
      so CAS can pick up the definition for the bean 'passwordPolicyAction'.
    -->

  <action-state id="passwordPolicyCheck">
    <evaluate expression="passwordPolicyAction" />
    <transition on="showWarning" to="passwordServiceCheck" />
    <transition on="success" to="sendTicketGrantingTicket" />
    <transition on="error" to="viewLoginForm" />
  </action-state>

  <action-state id="passwordServiceCheck">
    <evaluate expression="sendTicketGrantingTicketAction" />
    <transition to="passwordPostCheck" />
  </action-state>

  <decision-state id="passwordPostCheck">
    <if test="flowScope.service != null" then="warnPassRedirect" else="pwdWarningPostView" />
  </decision-state>

  <action-state id="warnPassRedirect">
    <evaluate expression="generateServiceTicketAction" />
    <transition on="success" to="pwdWarningPostView" />
    <transition on="error" to="generateLoginTicket" />
    <transition on="gateway" to="gatewayServicesManagementCheck" />
  </action-state>

  <end-state id="pwdWarningAbstractView">
    <on-entry>
      <set name="flowScope.passwordPolicyUrl" value="passwordPolicyAction.getPasswordPolicyUrl()" />
    </on-entry>
  </end-state>
  <end-state id="pwdWarningPostView" view="casWarnPassView" parent="#pwdWarningAbstractView" />
  <end-state id="casExpiredPassView" view="casExpiredPassView" parent="#pwdWarningAbstractView" />
  <end-state id="casMustChangePassView" view="casMustChangePassView" parent="#pwdWarningAbstractView" />
  <end-state id="casAccountDisabledView" view="casAccountDisabledView" />
  <end-state id="casAccountLockedView" view="casAccountLockedView" />
  <end-state id="casBadHoursView" view="casBadHoursView" />
  <end-state id="casBadWorkstationView" view="casBadWorkstationView" />
  <!-- LPPE transitions end here... -->

  <action-state id="generateLoginTicket">
        <evaluate expression="generateLoginTicketAction.generate(flowRequestContext)" />
    <transition on="generated" to="viewLoginForm" />
  </action-state>

  <view-state id="viewLoginForm" view="casLoginView" model="credentials">
        <binder>
            <binding property="username" />
            <binding property="password" />
        </binder>
        <on-entry>
            <set name="viewScope.commandName" value="'credentials'" />
        </on-entry>
    <transition on="submit" bind="true" validate="true" to="realSubmit">
            <evaluate expression="authenticationViaFormAction.doBind(flowRequestContext, flowScope.credentials)" />
        </transition>
  </view-state>

    <action-state id="realSubmit">
        <evaluate expression="authenticationViaFormAction.submit(flowRequestContext, flowScope.credentials, messageContext)" />
      <transition on="warn" to="determineIfTwoFactor" /> <!-- Changed for Duo Support -->
      <transition on="success" to="determineIfTwoFactor" /> <!-- Changed for Duo Support -->
      <transition on="error" to="generateLoginTicket" />
      <transition on="accountDisabled" to="casAccountDisabledView" />
      <transition on="mustChangePassword" to="casMustChangePassView" />
      <transition on="accountLocked" to="casAccountLockedView" />
      <transition on="badHours" to="casBadHoursView" />
      <transition on="badWorkstation" to="casBadWorkstationView" />
      <transition on="passwordExpired" to="casExpiredPassView" />
  </action-state>

<!-- Added for Duo support -->
  <action-state id="determineIfTwoFactor">
    <evaluate expression="determineIfTwoFactorAction" />
    <transition on="multiFactorNeeded" to="startDuoSecondFactorFlow" />
    <transition on="noMultiFactorNeeded" to="passwordPolicyCheck" />
    <transition on="error" to="generateLoginTicket" />
  </action-state>

  <action-state id="startDuoSecondFactorFlow">
    <evaluate expression="generateLoginTicketAction.generate(flowRequestContext)" />
    <transition on="generated" to="generateDuoCredentials" />
  </action-state>

  <action-state id="generateDuoCredentials">
    <evaluate expression="generateDuoCredentials.createDuoCredentials(flowRequestContext)" />
      <transition on="created" to="viewLoginFormTF" />
      <transition on="error" to="generateLoginTicket" />
  </action-state>

  <view-state id="viewLoginFormTF" view="casDuoLoginView" model="duoCredentials">
    <binder>
      <binding property="signedDuoResponse" />
    </binder>
    <on-entry>
      <evaluate expression="duoUtils.generateSignedRequest(flowScope.duoCredentials.getPrincipal().getId())" result="viewScope.sigRequest"/>
      <set name="viewScope.apiHost" value="duoConfiguration.getApiHost()" />
      <set name="viewScope.commandName" value="'duoCredentials'" />
    </on-entry>
    <transition on="submit" bind="true" validate="true" to="realSubmitTF">
      <evaluate expression="authenticationViaFormAction.doBind(flowRequestContext, flowScope.duoCredentials)" />
    </transition>
  </view-state>

  <action-state id="realSubmitTF">
    <evaluate expression="authenticationViaFormAction.submit(flowRequestContext, flowScope.duoCredentials, messageContext)" />
    <!--
    To enable LPPE on the 'warn' replace the below transition with:
    <transition on="warn" to="passwordPolicyCheck" />

    CAS will attempt to transition to the 'warn' when there's a 'renew' parameter
    and there exists a ticketGrantingId and a service for the incoming request.
    <transition on="warn" to="warn" />
      To enable LPPE on the 'success' replace the below transition with:
      <transition on="success" to="passwordPolicyCheck" />
    -->
    <transition on="warn" to="warn" />
    <transition on="success" to="sendTicketGrantingTicket" />
    <transition on="error" to="viewLoginForm" />
  </action-state>
<!-- END DUO SECOND FACTOR FLOW -->

  <action-state id="sendTicketGrantingTicket">
        <evaluate expression="sendTicketGrantingTicketAction" />
    <transition to="serviceCheck" />
  </action-state>

  <decision-state id="serviceCheck">
    <if test="flowScope.service != null" then="generateServiceTicket" else="viewGenericLoginSuccess" />
  </decision-state>

  <action-state id="generateServiceTicket">
        <evaluate expression="generateServiceTicketAction" />
    <transition on="success" to ="warn" />
    <transition on="error" to="generateLoginTicket" />
    <transition on="gateway" to="gatewayServicesManagementCheck" />
  </action-state>

    <action-state id="gatewayServicesManagementCheck">
        <evaluate expression="gatewayServicesManagementCheck" />
        <transition on="success" to="redirect" />
    </action-state>

    <action-state id="redirect">
        <evaluate expression="flowScope.service.getResponse(requestScope.serviceTicketId)" result-type="org.jasig.cas.authentication.principal.Response" result="requestScope.response" />
        <transition to="postRedirectDecision" />
    </action-state>

    <decision-state id="postRedirectDecision">
        <if test="requestScope.response.responseType.name() == 'POST'" then="postView" else="redirectView" />
    </decision-state>

  <!--
    the "viewGenericLogin" is the end state for when a user attempts to login without coming directly from a service.
    They have only initialized their single-sign on session.
  -->
  <end-state id="viewGenericLoginSuccess" view="casLoginGenericSuccessView" />

  <!--
    The "showWarningView" end state is the end state for when the user has requested privacy settings (to be "warned") to be turned on.  It delegates to a
    view defines in default_views.properties that display the "Please click here to go to the service." message.
  -->
  <end-state id="showWarningView" view="casLoginConfirmView" />

    <end-state id="postView" view="postResponseView">
        <on-entry>
            <set name="requestScope.parameters" value="requestScope.response.attributes" />
            <set name="requestScope.originalUrl" value="flowScope.service.id" />
        </on-entry>
    </end-state>

  <!--
    The "redirect" end state allows CAS to properly end the workflow while still redirecting
    the user back to the service required.
  -->
  <end-state id="redirectView" view="externalRedirect:${requestScope.response.url}" />

  <end-state id="viewServiceErrorView" view="viewServiceErrorView" />

    <end-state id="viewServiceSsoErrorView" view="viewServiceSsoErrorView" />

  <global-transitions>
        <!-- CAS-1023 This one is simple - redirects to a login page (same as renew) when 'ssoEnabled' flag is unchecked
             instead of showing an intermediate unauthorized view with a link to login page -->
        <transition to="viewLoginForm" on-exception="org.jasig.cas.services.UnauthorizedSsoServiceException"/>
        <transition to="viewServiceErrorView" on-exception="org.springframework.webflow.execution.repository.NoSuchFlowExecutionException" />
    <transition to="viewServiceErrorView" on-exception="org.jasig.cas.services.UnauthorizedServiceException" />
  </global-transitions>
</flow>
```

