/*******************************************************************************
 * Copyright (c) 2014 Abel G�mez.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Abel G�mez - initial API and implementation
 ******************************************************************************/
package io.github.abelgomez.kyanos.benchmarks;

import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.gmt.modisco.java.MethodDeclaration;

import io.github.abelgomez.kyanos.KyanosURI;
import io.github.abelgomez.kyanos.benchmarks.queries.JavaQueries;
import io.github.abelgomez.kyanos.benchmarks.util.MessageUtil;
import io.github.abelgomez.kyanos.core.KyanosResourceFactory;
import io.github.abelgomez.kyanos.map.impl.KyanosMapResourceImpl;

public class KyanosMapQuery {

	private static final Logger LOG = Logger.getLogger(KyanosMapQuery.class.getName());
	
	private static final String IN = "input";

	private static final String EPACKAGE_CLASS = "epackage_class";

	public static void main(String[] args) {
		Options options = new Options();
		
		Option inputOpt = OptionBuilder.create(IN);
		inputOpt.setArgName("INPUT");
		inputOpt.setDescription("Input Kyanos resource directory");
		inputOpt.setArgs(1);
		inputOpt.setRequired(true);
		
		Option inClassOpt = OptionBuilder.create(EPACKAGE_CLASS);
		inClassOpt.setArgName("CLASS");
		inClassOpt.setDescription("FQN of EPackage implementation class");
		inClassOpt.setArgs(1);
		inClassOpt.setRequired(true);
		
		options.addOption(inputOpt);
		options.addOption(inClassOpt);

		CommandLineParser parser = new PosixParser();
		
		try {
			CommandLine commandLine = parser.parse(options, args);
			
			URI uri = KyanosURI.createKyanosMapURI(new File(commandLine.getOptionValue(IN)));

			Class<?> inClazz = KyanosMapQuery.class.getClassLoader().loadClass(commandLine.getOptionValue(EPACKAGE_CLASS));
			inClazz.getMethod("init").invoke(null);
			
			ResourceSet resourceSet = new ResourceSetImpl();
			resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put(KyanosURI.KYANOS_MAP_SCHEME, KyanosResourceFactory.eINSTANCE);
			
			Resource resource = resourceSet.createResource(uri);
			
			Map<String, Object> loadOpts = new HashMap<String, Object>();
			resource.load(loadOpts);
			{
				LOG.log(Level.INFO, "Start query");
				long begin = System.currentTimeMillis();
				EList<MethodDeclaration> list = JavaQueries.getUnusedMethodsList(resource);
				long end = System.currentTimeMillis();
				LOG.log(Level.INFO, "End query");
				LOG.log(Level.INFO, MessageFormat.format("Query result (list) contains {0} elements", list.size()));
				LOG.log(Level.INFO, MessageFormat.format("Time spent: {0}", MessageUtil.formatMillis(end-begin)));
			}
			
			{
				LOG.log(Level.INFO, "Start query");
				long begin = System.currentTimeMillis();
				EList<MethodDeclaration> list = JavaQueries.getUnusedMethodsLoop(resource);
				long end = System.currentTimeMillis();
				LOG.log(Level.INFO, "End query");
				LOG.log(Level.INFO, MessageFormat.format("Query result (loops) contains {0} elements", list.size()));
				LOG.log(Level.INFO, MessageFormat.format("Time spent: {0}", MessageUtil.formatMillis(end-begin)));
			}
			
			if (resource instanceof KyanosMapResourceImpl) {
				KyanosMapResourceImpl.shutdownWithoutUnload((KyanosMapResourceImpl) resource); 
			} else {
				resource.unload();
			}
			
		} catch (ParseException e) {
			MessageUtil.showError(e.toString());
			MessageUtil.showError("Current arguments: " + Arrays.toString(args));
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar <this-file.jar>", options, true);
		} catch (Throwable e) {
			MessageUtil.showError(e.toString());
		}
	}
	

}
