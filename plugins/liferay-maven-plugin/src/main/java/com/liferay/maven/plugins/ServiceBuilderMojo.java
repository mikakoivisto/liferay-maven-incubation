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

import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.tools.servicebuilder.ServiceBuilder;
import com.liferay.portal.util.InitUtil;
import com.liferay.portal.util.PropsUtil;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Builds Liferay Service Builder services.
 *
 * @author Mika Koivisto
 * @author Thiago Moreira
 * @goal   build-service
 * @phase  process-sources
 */
public class ServiceBuilderMojo extends AbstractMojo {

	public void execute() throws MojoExecutionException {
		try {
			doExecute();
		}
		catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	protected void doExecute() throws Exception {
		File inputFile = new File(serviceFileName);

		if (!inputFile.exists()) {
			getLog().warn(inputFile.getAbsolutePath() + " does not exist");

			return;
		}

		getLog().info("Building from " + serviceFileName);

		PropsUtil.set("spring.configs", "META-INF/service-builder-spring.xml");
		PropsUtil.set(
			PropsKeys.RESOURCE_ACTIONS_READ_PORTLET_RESOURCES, "false");

		InitUtil.initWithSpring();

		new ServiceBuilder(
			serviceFileName, hbmFileName, ormFileName, modelHintsFileName,
			springFileName, springBaseFileName, null,
			springDynamicDataSourceFileName, springHibernateFileName,
			springInfrastructureFileName, springShardDataSourceFileName,
			apiDir, implDir, jsonFileName, null, sqlDir, sqlFileName,
			sqlIndexesFileName, sqlIndexesPropertiesFileName,
			sqlSequencesFileName, autoNamespaceTables, beanLocatorUtil,
			propsUtil, pluginName, null);
	}

	/**
	 * @parameter default-value="${basedir}/src/main/java"
	 * @required
	 */
	private String apiDir;

	/**
	 * @parameter default-value="true"
	 * @required
	 */
	private boolean autoNamespaceTables;

	/**
	 * @parameter default-value="com.liferay.util.bean.PortletBeanLocatorUtil"
	 * @required
	 */
	private String beanLocatorUtil;

	/**
	 * @parameter default-value=
				  "${basedir}/src/main/resources/META-INF/portlet-hbm.xml"
	 * @required
	 */
	private String hbmFileName;

	/**
	 * @parameter default-value="${basedir}/src/main/java"
	 * @required
	 */
	private String implDir;

	/**
	 * @parameter default-value=
				  "${basedir}/src/main/webapp/html/js/liferay/service.js"
	 * @required
	 */
	private String jsonFileName;

	/**
	 * @parameter default-value=
				  "${basedir}/src/main/resources/META-INF/portlet-model-hints.xml"
	 * @required
	 */
	private String modelHintsFileName;

	/**
	 * @parameter default-value=
				  "${basedir}/src/main/resources/META-INF/portlet-orm.xml"
	 * @required
	 */
	private String ormFileName;

	/**
	 * @parameter expression="${project.artifactId}"
	 * @required
	 */
	private String pluginName;

	/**
	 * @parameter default-value="com.liferay.util.service.ServiceProps"
	 * @required
	 */
	private String propsUtil;

	/**
	 * @parameter default-value="${basedir}/src/main/webapp/WEB-INF/service.xml"
	 * @required
	 */
	private String serviceFileName;

	/**
	 * @parameter default-value=
				  "${basedir}/src/main/resources/META-INF/base-spring.xml"
	 * @required
	 */
	private String springBaseFileName;

	/**
	 * @parameter default-value=
				  "${basedir}/src/main/resources/META-INF/dynamic-data-source-spring.xml"
	 * @required
	 */
	private String springDynamicDataSourceFileName;

	/**
	 * @parameter default-value=
				  "${basedir}/src/main/resources/META-INF/portlet-spring.xml"
	 * @required
	 */
	private String springFileName;

	/**
	 * @parameter default-value=
				  "${basedir}/src/main/resources/META-INF/hibernate-spring.xml"
	 * @required
	 */
	private String springHibernateFileName;

	/**
	 * @parameter default-value=
				  "${basedir}/src/main/resources/META-INF/infrastructure-spring.xml"
	 * @required
	 */
	private String springInfrastructureFileName;

	/**
	 * @parameter default-value=
				  "${basedir}/src/main/resources/META-INF/shard-data-source-spring.xml"
	 * @required
	 */
	private String springShardDataSourceFileName;

	/**
	 * @parameter default-value="${basedir}/src/main/webapp/WEB-INF/sql"
	 * @required
	 */
	private String sqlDir;

	/**
	 * @parameter default-value="tables.sql"
	 * @required
	 */
	private String sqlFileName;

	/**
	 * @parameter default-value="indexes.sql"
	 * @required
	 */
	private String sqlIndexesFileName;

	/**
	 * @parameter default-value="indexes.properties"
	 * @required
	 */
	private String sqlIndexesPropertiesFileName;

	/**
	 * @parameter default-value="sequences.sql"
	 * @required
	 */
	private String sqlSequencesFileName;

}