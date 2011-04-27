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

import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.tools.WSDDMerger;
import com.liferay.portal.util.FastDateFormatFactoryImpl;
import com.liferay.portal.util.FileImpl;
import com.liferay.portal.util.HtmlImpl;
import com.liferay.portal.xml.SAXReaderImpl;
import com.liferay.util.ant.Java2WsddTask;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * @author Mika Koivisto
 * @goal   build-wsdd
 */
public class WSDDBuilderMojo  extends AbstractMojo {

	public void execute() throws MojoExecutionException {
		try {
			initClassLoader();

			initPortal();

			doExecute();
		}
		catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	protected void doExecute() throws Exception {
		String _portletShortName = null;
		String _outputPath = resourcesDir;
		String _packagePath = null;

		if (!FileUtil.exists(serverConfigFileName)) {
			ClassLoader classLoader = getClass().getClassLoader();

			String serverConfigContent = StringUtil.read(
				classLoader,
				"com/liferay/portal/tools/dependencies/server-config.wsdd");

			FileUtil.write(serverConfigFileName, serverConfigContent);
		}

		Document doc = SAXReaderUtil.read(new File(serviceFileName), true);

		Element root = doc.getRootElement();

		String packagePath = root.attributeValue("package-path");

		Element portlet = root.element("portlet");
		Element namespace = root.element("namespace");

		if (portlet != null) {
			_portletShortName = portlet.attributeValue("short-name");
		}
		else {
			_portletShortName = namespace.getText();
		}

		_outputPath +=
			"/" + StringUtil.replace(packagePath, ".", "/") + "/service/http";

		_packagePath = packagePath;

		List<Element> entities = root.elements("entity");

		Iterator<Element> itr = entities.iterator();

		while (itr.hasNext()) {
			Element entity = itr.next();

			String entityName = entity.attributeValue("name");

			boolean remoteService = GetterUtil.getBoolean(
				entity.attributeValue("remote-service"), true);

			if (remoteService) {
				String className =
					_packagePath + ".service.http." + entityName + "ServiceSoap";

				String serviceName = StringUtil.replace(_portletShortName, " ", "_");

				if (!portalWsdd) {
					serviceName = "Plugin_" + serviceName;
				}
				else {
					if (!_portletShortName.equals("Portal")) {
						serviceName = "Portlet_" + serviceName;
					}
				}

				serviceName += ("_" + entityName + "Service");

				String[] wsdds = Java2WsddTask.generateWsdd(
					className, serviceName);

				FileUtil.write(
					new File(
						_outputPath + "/" + entityName + "Service_deploy.wsdd"),
					wsdds[0], true);

				FileUtil.write(
					new File(
						_outputPath + "/" + entityName +
						"Service_undeploy.wsdd"),
					wsdds[1], true);

				WSDDMerger.merge(
					_outputPath + "/" + entityName + "Service_deploy.wsdd",
					serverConfigFileName);
			}
		}
	}

	protected void initClassLoader() throws Exception {
		synchronized (ServiceBuilderMojo.class) {
			Class<?> clazz = getClass();

			URLClassLoader classLoader = (URLClassLoader)clazz.getClassLoader();

			Method method = URLClassLoader.class.getDeclaredMethod(
				"addURL", URL.class);

			method.setAccessible(true);

			for (Object object : project.getCompileClasspathElements()) {
				String path = (String)object;

				File file = new File(path);

				URI uri = file.toURI();

				method.invoke(classLoader, uri.toURL());
			}
		}
	}

	protected void initPortal() {
		FastDateFormatFactoryUtil fastDateFormatFactoryUtil =
			new FastDateFormatFactoryUtil();

		fastDateFormatFactoryUtil.setFastDateFormatFactory(
			new FastDateFormatFactoryImpl());

		FileUtil fileUtil = new FileUtil();

		fileUtil.setFile(new FileImpl());

		HtmlUtil htmlUtil = new HtmlUtil();

		htmlUtil.setHtml(new HtmlImpl());

		SAXReaderUtil saxReaderUtil = new SAXReaderUtil();

		saxReaderUtil.setSAXReader(new SAXReaderImpl());
	}

	/**
	 * @parameter default-value="false" expression="${portalWsdd}"
	 */
	private boolean portalWsdd;

	/**
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * @parameter expression="${basedir}/src/main/resources"
	 * @required
	 */
	private String resourcesDir;

	/**
	 * @parameter default-value="${basedir}/src/main/webapp/WEB-INF/server-config.wsdd" expression="${serverConfigFileName}"
	 * @required
	 */
	private String serverConfigFileName;

	/**
	 * @parameter default-value="${basedir}/src/main/webapp/WEB-INF/service.xml" expression="${serviceFileName}"
	 * @required
	 */
	private String serviceFileName;
}
