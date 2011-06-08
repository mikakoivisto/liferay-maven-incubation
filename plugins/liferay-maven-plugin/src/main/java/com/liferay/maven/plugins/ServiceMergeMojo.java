/**
 * 
 */
package com.liferay.maven.plugins;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.FileUtils.FilterWrapper;

/**
 * @author ladislav.gazo
 * 
 * @goal   service-merge
 * @phase  process-sources
 */
public class ServiceMergeMojo extends AbstractMojo {
	/**
	 * @parameter
	 * @required
	 */
	private File sourceDirectory;

	/**
	 * @parameter expression=
	 *			  "${project.build.directory}/${project.build.finalName}"
	 * @required
	 */
	private File webappDirectory;
		
	public void execute() throws MojoExecutionException, MojoFailureException {
		if(sourceDirectory == null || !sourceDirectory.exists()) {
			throw new MojoFailureException("No source directory specified = " + sourceDirectory);
		}
		
		FilterWrapper wra = new FilterWrapper() {
			
			@Override
			public Reader getReader(Reader arg0) {
				// TODO Auto-generated method stub
				return null;
			}
		};
		
		File[][] map = new File[3][2];
		
		map[0][0] = new File(sourceDirectory, "target" + File.separator + "classes");
		map[0][1] = new File(webappDirectory, "WEB-INF" + File.separator + "classes");
		
		map[1][0] = new File(sourceDirectory, "src" + File.separator + "main" + File.separator + "webapp" + File.separator + "html" + File.separator + "js" + File.separator + "liferay");
		map[1][1] = new File(webappDirectory, "js");

		map[2][0] = new File(sourceDirectory, "src" + File.separator + "main" + File.separator + "webapp" + File.separator + "WEB-INF" + File.separator + "sql");
		map[2][1] = new File(webappDirectory, "WEB-INF" + File.separator + "sql");
		
		for(File[] pair : map) {
			if(!pair[1].exists()) {
				pair[1].mkdirs();
			}
			
			try {
				FileUtils.copyDirectoryStructure(pair[0], pair[1]);
			} catch (IOException e) {
				throw new MojoFailureException("Unable to copy " + pair[0] + " to " + pair[1], e);
			}			
		}
	}

}
