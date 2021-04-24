
package com.example.customAuthNode;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.forgerock.openam.auth.node.api.AbstractNodeAmPlugin;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.plugins.PluginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class myCustomAuthNodePlugin extends AbstractNodeAmPlugin {

	static private String currentVersion = "1.0.0";
	
    /** 
     * Specify the Map of list of node classes that the plugin is providing. These will then be installed and
     *  registered at the appropriate times in plugin lifecycle.
     *
     * @return The list of node classes.
     */
	@Override
	protected Map<String, Iterable<? extends Class<? extends Node>>> getNodesByVersion() {
		return Collections.singletonMap(myCustomAuthNodePlugin.currentVersion, 
				Collections.singletonList(myCustomAuthNode.class));
	}

    /** 
     * Handle plugin installation. This method will only be called once, on first AM startup once the plugin
     * is included in the classpath. The {@link #onStartup()} method will be called after this one.
     * 
     * No need to implement this unless your AuthNode has specific requirements on install.
     */
	@Override
	public void onInstall() throws PluginException {
		super.onInstall();
	}

    /** 
     * Handle plugin startup. This method will be called every time AM starts, after {@link #onInstall()},
     * {@link #onAmUpgrade(String, String)} and {@link #upgrade(String)} have been called (if relevant).
     * 
     * No need to implement this unless your AuthNode has specific requirements on startup.
     *
     * @param startupType The type of startup that is taking place.
     */
	@Override
	public void onStartup() throws PluginException {
		super.onStartup();
	}

    /** 
     * This method will be called when the version returned by {@link #getPluginVersion()} is higher than the
     * version already installed. This method will be called before the {@link #onStartup()} method.
     * 
     * No need to implement this untils there are multiple versions of your auth node.
     *
     * @param fromVersion The old version of the plugin that has been installed.
     */	
	@Override
	public void upgrade(String fromVersion) throws PluginException {
		super.upgrade(fromVersion);
	}

    /** 
     * The plugin version. This must be in semver (semantic version) format.
     *
     * @return The version of the plugin.
     * @see <a href="https://www.osgi.org/wp-content/uploads/SemanticVersioning.pdf">Semantic Versioning</a>
     */
	@Override
	public String getPluginVersion() {
		return myCustomAuthNodePlugin.currentVersion;
	}
}
