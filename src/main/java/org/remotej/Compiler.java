package org.remotej;

import org.remotej.ddl.ErrorHandler;
import org.remotej.ddl.analyser.Parser;
import org.remotej.ddl.analyser.Scanner;
import org.remotej.ddl.analyser.SourceFile;
import org.remotej.ddl.drawer.Drawer;
import org.remotej.ddl.trees.Service;
import org.remotej.generator.Generator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * The main driver class for the RemoteJ compiler.
 */
public final class Compiler {

	private static String serverOutputDirectory = null;
	private static String clientOutputDirectory = null;
	private static String ddlFile = null;

	/**
	 * Message priority of &quot;error&quot;.
	 */
	public static final int MSG_ERR = 0;
	/**
	 * Message priority of &quot;warning&quot;.
	 */
	public static final int MSG_WARN = 1;
	/**
	 * Message priority of &quot;information&quot;.
	 */
	public static final int MSG_INFO = 2;
	/**
	 * Message priority of &quot;verbose&quot;.
	 */
	public static final int MSG_VERBOSE = 3;
	/**
	 * Message priority of &quot;debug&quot;.
	 */
	public static final int MSG_DEBUG = 4;

	/**
	 * Our current message output status. Follows Project.MSG_XXX.
	 */
	private static int msgOutputLevel = Compiler.MSG_INFO;

	private static final String DEFAULT_SERVEROUTPUT_DIRECTORY = "server";
	private static final String DEFAULT_CLIENTOUTPUT_DIRECTORY = "client";

	public static void debug(String message) {
		if (msgOutputLevel >= MSG_DEBUG) {
			System.err.println(message);
		}
	}

	public static void info(String message) {
		if (msgOutputLevel >= MSG_INFO) {
			System.err.println(message);
		}
	}

	/**
	 * Compile the source program to TAM machine code.
	 * 
	 * @param sdlFile
	 *            the name of the file containing the source program.
	 * @param showingAST
	 *            true iff the AST is to be displayed after contextual analysis
	 *            (not currently implemented).
	 */
	private void compileProgram(String sdlFile, boolean showingAST) {

		if (getRemoteJVersion() != null) {
			System.out.println(getRemoteJVersion());
		}
		debug("Syntactic Analysis ...");
		SourceFile source = new SourceFile(sdlFile);

		Scanner scanner = new Scanner(source);
		ErrorHandler reporter = new ErrorHandler();
		Parser parser = new Parser(scanner, reporter);
		Drawer drawer = new Drawer();
		Generator generator = new Generator(reporter);

		Service theAST = parser.parseService();
		if (reporter.getNumErrors() == 0) {
			if (showingAST) {
				drawer.draw(theAST);
			}

			if (reporter.getNumErrors() == 0) {
				debug("Code Generation ...");
				if (serverOutputDirectory != null) {
					generator.setServerOutputDirectory(serverOutputDirectory);
				} else {
					generator
							.setServerOutputDirectory(DEFAULT_SERVEROUTPUT_DIRECTORY);
				}
				if (clientOutputDirectory != null) {
					generator.setClientOutputDirectory(clientOutputDirectory);
				} else {
					generator
							.setClientOutputDirectory(DEFAULT_CLIENTOUTPUT_DIRECTORY);
				}
				generator.generate(theAST);
			}
		}

		if (reporter.getNumWarnings() > 0) {
			System.out.println(reporter.getNumWarnings() + " warning(s)");
		}

		if (reporter.getNumErrors() > 0) {
			System.out.println(reporter.getNumErrors() + " error(s)");
		}

		boolean successful = (reporter.getNumErrors() == 0);
		if (successful) {
			// encoder.saveObjectProgram(objectName);
			System.out.println("Compilation was successful.");
		} else {
			System.out.println("Compilation was unsuccessful.");
		}
	}

	private void processArgs(String[] args) {
		// cycle through given args

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			if (arg.equals("-help") || arg.equals("-h")) {
				printUsage();
				System.exit(1);
			} else if (arg.equals("-version") || arg.equals("-v")) {
				printVersion();
				System.exit(1);
			} else if (arg.equals("-debug") || arg.equals("-d")) {
				printVersion();
				msgOutputLevel = Compiler.MSG_DEBUG;
			} else if (arg.equals("-server") || arg.equals("-s")) {
				if (serverOutputDirectory != null) {
					throw new CompilerException(
							"Only one server output directory may be specified.");
				}
				try {
					serverOutputDirectory = args[++i];
				} catch (ArrayIndexOutOfBoundsException aioobe) {
					throw new CompilerException(
							"You must specify a directory name when"
									+ " using the -server or -s argument");
				}
			} else if (arg.equals("-client") || arg.equals("-c")) {
				if (clientOutputDirectory != null) {
					throw new CompilerException(
							"Only one client output directory may be specified.");
				}
				try {
					clientOutputDirectory = args[++i];
				} catch (ArrayIndexOutOfBoundsException aioobe) {
					throw new CompilerException(
							"You must specify a directory name when"
									+ " using the -client or -c argument");
				}
			} else if (arg.startsWith("-")) {
				// we don't have any more args to recognize!
				String msg = "Unknown argument: " + arg;
				System.out.println(msg);
				printUsage();
				throw new CompilerException("");
			} else {
				if (ddlFile != null) {
					throw new CompilerException(
							"Only one ddl file may be specified.");
				}
				ddlFile = arg;
			}
		}

		// if DDL File was not specified on the command line,
		if (ddlFile == null) {
			throw new CompilerException("DDL file not specified.");
		}

		// make sure buildfile exists
		if (!new File(ddlFile).exists()) {
			System.out.println("DDL file: " + ddlFile + " does not exist!");
			throw new CompilerException("Compile failed");
		}

		// make sure it's not a directory (this falls into the ultra
		// paranoid lets check everything category

		if (new File(ddlFile).isDirectory()) {
			System.out.println("DDL file: " + ddlFile + " is a directory");
			throw new CompilerException("Compile failed");
		}

		debug("DDL file: " + ddlFile);
	}

	/**
	 * Prints the usage information for this class to <code>System.out</code>.
	 */
	private static void printUsage() {
		String lSep = System.getProperty("line.separator");
		StringBuffer msg = new StringBuffer();
		msg.append("remotej [protocolOptions] DDL file").append(lSep);
		msg.append("ProtocolOptions: ").append(lSep);
		msg.append("  -help, -h                  print this message").append(
				lSep);
		msg
				.append(
						"  -version, -v               print the version information and exit")
				.append(lSep);
		msg.append("  -debug,  -d                print debugging information")
				.append(lSep);
		msg
				.append(
						"  -server, -s <directory>    directory to store generated server files")
				.append(lSep);
		msg
				.append(
						"  -client, -c <directory>    directory to store generated client files")
				.append(lSep);
		System.out.println(msg.toString());
	}

	/**
	 * Prints the Ant version information to <code>System.out</code>.
	 * 
	 * @throws CompilerException
	 *             if the version information is unavailable
	 */
	private static void printVersion() throws CompilerException {
		System.out.println(getRemoteJVersion());
	}

	private static String remoteJVersion = null;

	/**
	 * Returns the Ant version information, if available. Once the information
	 * has been loaded once, it's cached and returned from the cache on future
	 * calls.
	 * 
	 * @return the Ant version information as a String (always non-<code>null</code>)
	 * @throws CompilerException
	 *             if the version information is unavailable
	 */
	public static synchronized String getRemoteJVersion()
			throws CompilerException {
		if (remoteJVersion == null) {
			try {
				Properties props = new Properties();
				InputStream in = Compiler.class
						.getResourceAsStream("/org/remotej/version.txt");
				props.load(in);
				in.close();

				StringBuffer msg = new StringBuffer();
				msg.append("\n");
				msg.append("RemoteJ version ");
				msg.append(props.getProperty("VERSION"));
				msg.append(" b");
				msg.append(props.getProperty("BUILD"));
				msg.append(" Compiled on ");
				msg.append(props.getProperty("DATE"));
				msg.append("\n");
				remoteJVersion = msg.toString();
			} catch (IOException ioe) {
				// throw new CompilerException("Could not load the version
				// information:"
				// + ioe.getMessage());
			} catch (NullPointerException npe) {
				// throw new CompilerException("Could not load the version
				// information.");
			}
		}
		return remoteJVersion;
	}

	/**
	 * Prints the message of the Throwable if it (the message) is not
	 * <code>null</code>.
	 * 
	 * @param t
	 *            Throwable to print the message of. Must not be
	 *            <code>null</code>.
	 */
	private static void printMessage(Throwable t) {
		String message = t.getMessage();
		if (message != null) {
			System.err.println(message);
		}
	}

	/**
	 * RemoteJ compiler main program.
	 * 
	 * @param args
	 *            the only command-line argument to the program specifies the
	 *            source filename.
	 */
	public static void main(String[] args) {
		Compiler c = new Compiler();
		c.startRemoteJ(args);
	}

	private void startRemoteJ(String[] args) {

		try {
			processArgs(args);
		} catch (Throwable exc) {
			printMessage(exc);
			System.exit(1);
		}

		compileProgram(ddlFile, false);
	}
}
