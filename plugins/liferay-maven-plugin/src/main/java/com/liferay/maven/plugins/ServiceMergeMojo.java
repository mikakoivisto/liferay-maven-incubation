/**
 * 
 */
package com.liferay.maven.plugins;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

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
				
		File[][] map = new File[3][2];
		FileFilter[] filter = new FileFilter[3];
		
		map[0][0] = new File(sourceDirectory, "target" + File.separator + "classes");
		map[0][1] = new File(webappDirectory, "WEB-INF" + File.separator + "classes");
		filter[0] = new FileFilter() {
//			<exclude>META-INF/*</exclude>
//			<exclude>service.properties</exclude>
//			<exclude>**/*Impl.class</exclude>
//			<exclude>**/*JSONSerializer.class</exclude>
//			<exclude>**/*ServiceSoap.class</exclude>

			public boolean accept(File pathname) {
				if(getLog().isDebugEnabled()) {
					getLog().debug("[filter] pathname = " + pathname + ", name = " + pathname.getName());
				}

				if(pathname.isDirectory() ||
						pathname.getAbsolutePath().contains("META-INF") ||
						pathname.getName().compareTo("service.properties") == 0 ||
						pathname.getName().endsWith("Impl.class") || 
						pathname.getName().endsWith("JSONSerializer.class") ||
						pathname.getName().endsWith("ServiceSoap.class")) {
					return true;
				}
						
				return false;
			}
		}; 
		
		map[1][0] = new File(sourceDirectory, "src" + File.separator + "main" + File.separator + "webapp" + File.separator + "html" + File.separator + "js" + File.separator + "liferay");
		map[1][1] = new File(webappDirectory, "js");

		map[2][0] = new File(sourceDirectory, "src" + File.separator + "main" + File.separator + "webapp" + File.separator + "WEB-INF");
		map[2][1] = new File(webappDirectory, "WEB-INF");
		
		
		int i = 0;
		for(File[] pair : map) {
			if(!pair[1].exists()) {
				pair[1].mkdirs();
			}
			
			try {
				if(filter[i] != null) {
					if(getLog().isDebugEnabled()) {
						getLog().debug("Using filter to copy from = " + pair[0] + " to = " + pair[1]);
					}
					org.apache.commons.io.FileUtils.copyDirectory(pair[0], pair[1], filter[i]);
				} else {
					if(getLog().isDebugEnabled()) {
						getLog().debug("Not-using filter to copy from = " + pair[0] + " to = " + pair[1]);
					}					
					org.apache.commons.io.FileUtils.copyDirectory(pair[0], pair[1]);
				}
//				FileUtils.copyDirectoryStructure(pair[0], pair[1]);
			} catch (IOException e) {
				throw new MojoFailureException("Unable to copy " + pair[0] + " to " + pair[1], e);
			}		
			i++;
		}
	}
	
}
