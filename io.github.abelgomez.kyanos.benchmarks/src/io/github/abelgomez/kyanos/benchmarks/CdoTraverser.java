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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;
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
import org.eclipse.emf.cdo.session.CDOSession;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import io.github.abelgomez.kyanos.benchmarks.cdo.EmbeddedCDOServer;
import io.github.abelgomez.kyanos.benchmarks.util.MessageUtil;

public class CdoTraverser {

	private static final Logger LOG = Logger.getLogger(CdoTraverser.class.getName());

	private static final String IN = "input";

	private static final String REPO_NAME = "reponame";

	private static final String EPACKAGE_CLASS = "epackage_class";


	public static void main(String[] args) {
		Options options = new Options();
		
		Option inputOpt = OptionBuilder.create(IN);
		inputOpt.setArgName("INPUT");
		inputOpt.setDescription("Input CDO resource directory");
		inputOpt.setArgs(1);
		inputOpt.setRequired(true);
		
		Option inClassOpt = OptionBuilder.create(EPACKAGE_CLASS);
		inClassOpt.setArgName("CLASS");
		inClassOpt.setDescription("FQN of EPackage implementation class");
		inClassOpt.setArgs(1);
		inClassOpt.setRequired(true);
		
		Option repoOpt = OptionBuilder.create(REPO_NAME);
		repoOpt.setArgName("REPO_NAME");
		repoOpt.setDescription("CDO Repository name");
		repoOpt.setArgs(1);
		repoOpt.setRequired(true);
		
		options.addOption(inputOpt);
		options.addOption(inClassOpt);
		options.addOption(repoOpt);

		CommandLineParser parser = new PosixParser();
		
		try {
			CommandLine commandLine = parser.parse(options, args);

			String repositoryDir = commandLine.getOptionValue(IN);
			String repositoryName = commandLine.getOptionValue(REPO_NAME);
			
			Class<?> inClazz = CdoTraverser.class.getClassLoader().loadClass(commandLine.getOptionValue(EPACKAGE_CLASS));
			inClazz.getMethod("init").invoke(null);
			
			EmbeddedCDOServer server = new EmbeddedCDOServer(repositoryDir, repositoryName);
			try {
				server.run();
				CDOSession session = server.openSession();
				CDOTransaction transaction = session.openTransaction();
				Resource resource = transaction.getRootResource();

				LOG.log(Level.INFO, "Start counting");
				int count = 0;
				long begin = System.currentTimeMillis();
				for (Iterator<EObject> iterator = resource.getAllContents(); iterator.hasNext(); iterator.next(), count++);
				long end = System.currentTimeMillis();
				LOG.log(Level.INFO, "End counting");
				LOG.log(Level.INFO, MessageFormat.format("Resource {0} contains {1} elements", resource.getURI(), count));
				LOG.log(Level.INFO, MessageFormat.format("Time spent: {0}", MessageUtil.formatMillis(end-begin)));
				
				transaction.close();
				session.close();
			} finally {
				server.stop();
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
