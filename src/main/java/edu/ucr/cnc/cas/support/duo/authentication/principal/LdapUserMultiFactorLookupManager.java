package edu.ucr.cnc.cas.support.duo.authentication.principal;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.util.LdapUtils;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import java.util.List;

/**
 * An implementation of {@link UserMultiFactorLookupManager} that uses attributes in LDAP to make
 * the determination.
 *
 * @author Michael Kennedy
 * @version 1.1
 */
public class LdapUserMultiFactorLookupManager implements UserMultiFactorLookupManager {

    private LdapContextSource contextSource;
    private String filter;
    private String searchBase;
    private String multiFactorAttributeName;

    private int scope = SearchControls.SUBTREE_SCOPE;

    /** The amount of time to wait. */
    private int timeout = DEFAULT_TIMEOUT;

    /** The default maximum number of results to return. */
    private static final int DEFAULT_MAX_NUMBER_OF_RESULTS = 1;

    /** The default timeout. */
    private static final int DEFAULT_TIMEOUT = 1000;

    /** The default value to compare to. **/
    private String multiFactorAttributeValue = "YES";

    @Override
    public boolean getMFARequired(String username) {

        String searchFilter = LdapUtils.getFilterWithValues(getFilter(), username);

        String result = "";

        LdapTemplate ldapTemplate = new LdapTemplate(this.contextSource);

        List secrets = ldapTemplate.search(
                getSearchBase(), searchFilter, getSearchControls(),

                new AttributesMapper() {
                    public Object mapFromAttributes(final Attributes attrs)
                            throws NamingException {
                        final Attribute attribute = attrs.get(multiFactorAttributeName);

                        if(attribute != null) {
                            return attribute.get();
                        }

                        return false;
                    }

                }
        );

        result = (String) secrets.get(0);

        return this.multiFactorAttributeValue.equalsIgnoreCase(result);

    }

    public LdapContextSource getContextSource() {
        return contextSource;
    }

    public void setContextSource(LdapContextSource contextSource) {
        this.contextSource = contextSource;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getSearchBase() {
        return searchBase;
    }

    public void setSearchBase(String searchBase) {
        this.searchBase = searchBase;
    }

    public String getMultiFactorAttributeName() {
        return multiFactorAttributeName;
    }

    public void setMultiFactorAttributeName(String multiFactorAttributeName) {
        this.multiFactorAttributeName = multiFactorAttributeName;
    }

    public String getMultiFactorAttributeValue() {
        return multiFactorAttributeValue;
    }

    public void setMultiFactorAttributeValue(String multiFactorAttributeValue) {
        this.multiFactorAttributeValue = multiFactorAttributeValue;
    }
    /**
     * Generates the SearchControls for the LDAP query to be executed
     *
     * @return a SearchControls object containing the default search scope, time out, attributes to return and the max number of entries to return
     */

    protected final SearchControls getSearchControls() {
        final SearchControls constraints = new SearchControls();

        constraints.setSearchScope(this.scope);
        constraints.setReturningAttributes(new String[]{this.multiFactorAttributeName});
        constraints.setTimeLimit(this.timeout);
        constraints.setCountLimit(DEFAULT_MAX_NUMBER_OF_RESULTS);
        return constraints;
    }
}
