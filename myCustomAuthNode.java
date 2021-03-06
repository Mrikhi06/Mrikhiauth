
package com.example.customAuthNode;

import static org.forgerock.openam.auth.node.api.SharedStateConstants.PASSWORD;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.AbstractDecisionNode;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.core.realms.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;
import com.iplanet.sso.SSOException;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.idm.IdType;
import com.sun.identity.idm.IdUtils;

/**
 * A node that checks to see if zero-page login headers have specified username and whether that username is in a group
 * permitted to use zero-page login headers.
 */
@Node.Metadata(outcomeProvider  = AbstractDecisionNode.OutcomeProvider.class,
               configClass      = myCustomAuthNode.Config.class)
public class myCustomAuthNode extends AbstractDecisionNode {

    private final Pattern DN_PATTERN = Pattern.compile("^[a-zA-Z0-9]=([^,]+),");
    private final Logger logger = LoggerFactory.getLogger(myCustomAuthNode.class);
    private final Config config;
    private final Realm realm;

    /**
     * Configuration for the node.
     */
    public interface Config {
        /**
         * The header name for zero-page login that will contain the identity's username.
         */
        @Attribute(order = 100)
        default String usernameHeader() {
            return "X-OpenAM-Username";
        }

        /**
         * The header name for zero-page login that will contain the identity's password.
         */
        @Attribute(order = 200)
        default String passwordHeader() {
            return "X-OpenAM-Password";
        }

        /**
         * The group name (or fully-qualified unique identifier) for the group that the identity must be in.
         */
        @Attribute(order = 300)
        default String groupName() {
            return "zero-page-login";
        }
    }


    /**
     * Create the node using Guice injection. Just-in-time bindings can be used to obtain instances of other classes
     * from the plugin.
     *
     * @param config The service config.
     * @param realm The realm the node is in.
     * @throws NodeProcessException If the configuration was not valid.
     */
    @Inject
    public myCustomAuthNode(@Assisted Config config, @Assisted Realm realm) throws NodeProcessException {
        this.config = config;
        this.realm = realm;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        boolean hasUsername = context.request.headers.containsKey(config.usernameHeader());
        boolean hasPassword = context.request.headers.containsKey(config.passwordHeader());

        if (!hasUsername || !hasPassword) {
            return goTo(false).build();
        }

        String username = context.request.headers.get(config.usernameHeader()).get(0);
        String password = context.request.headers.get(config.passwordHeader()).get(0);
        AMIdentity userIdentity = IdUtils.getIdentity(username, realm.asDN());
        try {
            if (userIdentity != null && userIdentity.isExists() && userIdentity.isActive()
                    && isMemberOfGroup(userIdentity, config.groupName())) {
                return goTo(true)
                        .replaceSharedState(context.sharedState.copy().put(USERNAME, username))
                        .replaceTransientState(context.transientState.copy().put(PASSWORD, password))
                        .build();
            }
        } catch (IdRepoException | SSOException e) {
            logger.warn("Error locating user '{}' ", username, e);
        }
        return goTo(false).build();
    }

    private boolean isMemberOfGroup(AMIdentity userIdentity, String groupName) {
        try {
            Set<String> userGroups = userIdentity.getMemberships(IdType.GROUP);
            for (String group : userGroups) {
                if (groupName.equals(group)) {
                    return true;
                }
                Matcher dnMatcher = DN_PATTERN.matcher(group);
                if (dnMatcher.find() && dnMatcher.group(1).equals(groupName)) {
                    return true;
                }
            }
        } catch (IdRepoException | SSOException e) {
            logger.warn("Could not load groups for user {}", userIdentity);
        }
        return false;
    }
}
