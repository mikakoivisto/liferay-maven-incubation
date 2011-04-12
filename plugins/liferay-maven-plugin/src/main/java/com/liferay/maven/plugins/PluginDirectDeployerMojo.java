/**
 * Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.maven.plugins;

import com.liferay.portal.bean.BeanLocatorImpl;
import com.liferay.portal.kernel.bean.PortalBeanLocatorUtil;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.tools.deploy.HookDeployer;
import com.liferay.portal.tools.deploy.LayoutTemplateDeployer;
import com.liferay.portal.tools.deploy.PortletDeployer;
import com.liferay.portal.tools.deploy.ThemeDeployer;
import com.liferay.portal.util.FastDateFormatFactoryImpl;
import com.liferay.portal.util.FileImpl;
import com.liferay.portal.util.HtmlImpl;
import com.liferay.portal.util.InitUtil;
import com.liferay.portal.util.PortalImpl;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.xml.SAXReaderImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.codehaus.plexus.components.io.fileselectors.IncludeExcludeFileSelector;

/**
 * @author Mika Koivisto
 * @goal   direct-deploy
 */
public class PluginDirectDeployerMojo extends AbstractMojo {

	public void execute() throws MojoExecutionException {
		try {
			doExecute();
		}
		catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	protected void doExecute() throws Exception {
		if (warFile.exists()) {
			getLog().info(
				"Direct deploying " + warFileName);

			getLog().debug("appServerType: " + appServerType);
			getLog().debug("baseDir: " + baseDir);
			getLog().debug("deployDir: " + deployDir.getAbsolutePath());
			getLog().debug("jbossPrefix: " + jbossPrefix);
			getLog().debug("pluginType: " + pluginType);
			getLog().debug("unpackWar: " + unpackWar);

			_preparePortalDependencies();

			System.setProperty(
				"liferay.lib.portal.dir",
				workDir.getAbsolutePath() + "/WEB-INF/lib");
			System.setProperty("deployer.base.dir", baseDir);
			System.setProperty(
				"deployer.dest.dir", deployDir.getAbsolutePath());
			System.setProperty("deployer.app.server.type", appServerType);
			System.setProperty(
				"deployer.unpack.war", String.valueOf(unpackWar));
			System.setProperty("deployer.file.pattern", warFileName);

			_initPortal();

			if (_PLUGIN_TYPE_HOOK.equals(pluginType)) {
				doDeployHook();
			}
			else if (_PLUGIN_TYPE_LAYOUTTPL.equals(pluginType)) {
				doDeployLayouttpl();
			}
			else if (_PLUGIN_TYPE_PORTLET.equals(pluginType)) {
				doDeployPortlet();
			}
			else if (_PLUGIN_TYPE_THEME.equals(pluginType)) {
				doDeployTheme();
			}
		}
		else {
			getLog().error(warFileName + " does not exist");

			throw new FileNotFoundException(warFileName + " does not exist!");
		}
	}

	protected void doDeployHook() throws Exception {
		List<String> wars = new ArrayList<String>();
		List<String> jars = new ArrayList<String>();

		String libPathPrefix = workDir.getAbsolutePath() + "/WEB-INF/lib";

		jars.add(libPathPrefix + "/util-java.jar");

		new HookDeployer(wars, jars);
	}

	protected void doDeployLayouttpl() throws Exception {
		List<String> wars = new ArrayList<String>();
		List<String> jars = new ArrayList<String>();

		new LayoutTemplateDeployer(wars, jars);
	}

	protected void doDeployPortlet() throws Exception {
		String tldPathPrefix = workDir.getAbsolutePath() + "/WEB-INF/tld";

		System.setProperty(
			"deployer.aui.taglib.dtd", tldPathPrefix + "/liferay-aui.tld");
		System.setProperty(
			"deployer.portlet.taglib.dtd",
			tldPathPrefix + "/liferay-portlet.tld");
		System.setProperty(
			"deployer.portlet-ext.taglib.dtd",
			tldPathPrefix + "/liferay-portlet-ext.tld");
		System.setProperty(
			"deployer.security.taglib.dtd",
			tldPathPrefix + "/liferay-security.tld");
		System.setProperty(
			"deployer.theme.taglib.dtd",
			tldPathPrefix + "/liferay-theme.tld");
		System.setProperty(
			"deployer.ui.taglib.dtd",
			tldPathPrefix + "/liferay-ui.tld");
		System.setProperty(
			"deployer.util.taglib.dtd",
			tldPathPrefix + "/liferay-util.tld");
		System.setProperty(
			"deployer.custom.portlet.xml", String.valueOf(customPortletXml));

		List<String> jars = new ArrayList<String>();

		String libPathPrefix = workDir.getAbsolutePath() + "/WEB-INF/lib";

		jars.add(libPathPrefix + "/util-bridges.jar");
		jars.add(libPathPrefix + "/util-java.jar");
		jars.add(libPathPrefix + "/util-taglib.jar");

		List<String> wars = new ArrayList<String>();

		new PortletDeployer(wars, jars);
	}

	protected void doDeployTheme() throws Exception {
		String tldPathPrefix = workDir.getAbsolutePath() + "/WEB-INF/tld";

		System.setProperty(
			"deployer.theme.taglib.dtd", tldPathPrefix + "/liferay-theme.tld");
		System.setProperty(
			"deployer.util.taglib.dtd", tldPathPrefix + "/liferay-util.tld");

		List<String> jars = new ArrayList<String>();

		String libPathPrefix = workDir.getAbsolutePath() + "/WEB-INF/lib";

		jars.add(libPathPrefix + "/util-java.jar");
		jars.add(libPathPrefix + "/util-taglib.jar");

		List<String> wars = new ArrayList<String>();

		new ThemeDeployer(wars, jars);
	}

	private void _initPortal() {
		InitUtil.init();

		new FastDateFormatFactoryUtil().setFastDateFormatFactory(
			new FastDateFormatFactoryImpl());
		new FileUtil().setFile(new FileImpl());
		new HtmlUtil().setHtml(new HtmlImpl());
		PortalBeanLocatorUtil.setBeanLocator(
			new BeanLocatorImpl(null, null));
		new PortalUtil().setPortal(new PortalImpl());
		new SAXReaderUtil().setSAXReader(new SAXReaderImpl());		
	}

	private void _preparePortalDependencies() throws Exception {
		Artifact artifact = artifactFactory.createArtifact(
			"com.liferay.portal", "portal-web", liferayVersion, "", "war");

		artifactResolver.resolve(
			artifact, remoteArtifactRepositories, localArtifactRepository);

		if (!workDir.exists()) {
			workDir.mkdirs();
		}

		UnArchiver unArchiver = archiverManager.getUnArchiver(
			artifact.getFile());

		unArchiver.setDestDirectory(workDir);
		unArchiver.setSourceFile(artifact.getFile());

		IncludeExcludeFileSelector includeExcludeFileSelector =
			new IncludeExcludeFileSelector();

		includeExcludeFileSelector.setExcludes(new String[]{});
		includeExcludeFileSelector.setIncludes(
			new String[] {"WEB-INF/tld/**", "WEB-INF/lib/**"});

		unArchiver.setFileSelectors(
			new FileSelector[] {includeExcludeFileSelector});

		unArchiver.extract();
	}

	/**
	 * @parameter expression="${appServerType}" default-value="tomcat"
	 * @required
	 */
	private String appServerType;

	/**
	 * @component
	 */
	private ArchiverManager archiverManager;

	/**
	 * @component
	 */
	private ArtifactFactory artifactFactory;

	/**
	 * @component
	 */
	private ArtifactResolver artifactResolver;

	/**
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private String baseDir;

	/**
	 * @parameter expression="${customPortletXml}" default-value="false"
	 * @required
	 */
	private boolean customPortletXml;

	/**
	 * @parameter expression="${deployDir}"
	 * @required
	 */
	private File deployDir;

	/**
	 * @parameter expression="${jbossPrefix}" default-value=""
	 * @required
	 */
	private String jbossPrefix;

	/**
	 * @parameter expression="${liferayVersion}"
	 * @required
	 */
	private String liferayVersion;

	/**
	 * @parameter expression="${localRepository}"
	 * @readonly
	 * @required
	 */
	private ArtifactRepository localArtifactRepository;

	/**
	 * @parameter expression="${pluginType}" default-value="portlet"
	 * @required
	 */
	private String pluginType;

	/**
	 * @parameter expression="${project.remoteArtifactRepositories}"
	 * @readonly
	 * @required
	 */
	private List remoteArtifactRepositories;

	/**
	 * @parameter expression="${unpackWar}" default-value="true"
	 * @required
	 */
	private boolean unpackWar;

	/**
	 * @parameter expression=
	 *			  "${project.build.directory}/${project.build.finalName}.war"
	 * @required
	 */
	private File warFile;

	/**
	 * @parameter expression="${project.build.finalName}.war"
	 * @required
	 */
	private String warFileName;

	/**
	 * @parameter expression="${project.build.directory}/liferay-work"
	 * @required
	 */
	private File workDir;

	private static final String _PLUGIN_TYPE_EXT = "ext";
	private static final String _PLUGIN_TYPE_HOOK = "hook";
	private static final String _PLUGIN_TYPE_LAYOUTTPL = "layouttpl";
	private static final String _PLUGIN_TYPE_PORTLET = "portlet";
	private static final String _PLUGIN_TYPE_THEME = "theme";

}
