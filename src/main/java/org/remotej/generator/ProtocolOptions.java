package org.remotej.generator;

import java.util.Vector;

public final class ProtocolOptions {

    public static final String PROTOCOL = "protocol";

    private final Vector<_Options> ops = new Vector<_Options>();

    public ProtocolOptions() {
    }

    public void addOption(String name, String value, int lineNumber) throws DuplicateOptionException {
        if (getOptionValue(name) != null) {
            throw new DuplicateOptionException("Option has already been declared");
        }
        _Options o = new _Options();
        o.name = name;
        o.value = value;
        o.lineNo = lineNumber;
        ops.add(o);
    }

    @SuppressWarnings("unchecked")
    public String getOptionValue(String name) {
        for (_Options options : ops) {
            if (options.name.equals(name)) {
                return options.value;
            }
        }
        return null;
    }

    public String getAndRemoveOptionValue(String name) {
        for (_Options options : ops) {
            if (options.name.equals(name)) {
                String s = options.value;
                ops.remove(s);
                return s;
            }
        }
        return null;
    }

    public void removeOption(String name) {
        ops.remove(name);
    }

    public int getLineNo(String name) {
        for (_Options options : ops) {
            if (options.name.equals(name)) {
                return options.lineNo;
            }
        }
        return -1;
    }

    /*
    public final void validateOptions() throws OptionException {
       ProtocolStatement.PROTOCOL_TYPE p = getOptionValue(OPTIONS.protocol);
       if (p == null) {
          throw new OptionException("protocol has not been declared");
       }
       //RMI
       registryName = getOptionValue(OPTIONS.registryName);
       registryHost = getOptionValue(OPTIONS.registryHost);
       registryPort = getOptionValue(OPTIONS.registryPort);
       runEmbeddedRegistry = getOptionValue(OPTIONS.runEmbeddedRegistry);
       lease = getOptionValue(OPTIONS.lease);
       //JMS
       destinationClass  = getOptionValue(OPTIONS.destinationClass);
       brokerURL         = getOptionValue(OPTIONS.brokerURL);
       sendTopic         = getOptionValue(OPTIONS.sendTopic);
       receiveQueue      = getOptionValue(OPTIONS.receiveQueue);
       serverThreads     = getOptionValue(OPTIONS.serverThreads);
       initialContextFactory = getOptionValue(OPTIONS.initialContextFactory);
       persist           = getOptionValue(OPTIONS.persist);
       receiveTimeout    = getOptionValue(OPTIONS.receiveTimeout);

       switch (p) {
          case JMS:
             checkJMSOptions();
             break;
          case RMI:
             checkRmiOptions();
             break;
       }
    }

    private void checkJMSOptions() throws OptionException {
       if (registryName != null || registryHost != null || registryPort != null ||
          runEmbeddedRegistry != null || lease != null) {
          throw new OptionException("option is not valid for this protocol");
       }

       if (destinationClass == null) {
          throw new OptionException("destinationClass has not been set");
       }
       if (brokerURL == null) {
          throw new OptionException("brokerURL has not been set");
       }
       if (sendTopic == null) {
          throw new OptionException("sendTopic has not been set");
       }
       if (receiveQueue == null) {
          throw new OptionException("receiveQueue has not been set");
       }
       if (serverThreads == null) {
          try {
             addOption(OPTIONS.serverThreads, 2, 0);
          } catch (DuplicateOptionException e) {
          }
       }
       if (initialContextFactory == null) {
          throw new OptionException("initialcontextFactory has not been set");
       }
       if (persist = null) {
          try {
             addOption(OPTIONS.persist, true, 0);
          } catch (DuplicateOptionException e) {
          }
       }
       if (receiveTimeout == null) {
          try {
             addOption(OPTIONS.receiveTimeout, 0, 0);
          } catch (DuplicateOptionException e) {

          }
       }
    }

    @SuppressWarnings({"EmptyCatchBlock"})
    private void checkRmiOptions() throws OptionException {
       if (destinationClass != null || brokerURL != null || sendTopic != null ||
          receiveQueue != null || serverThreads != null || initialContextFactory != null ||
          persist != null || receiveTimeout != null) {
          throw new OptionException("option is not valid for this protocol");
       }
       // check for required protocolOptions
       if (registryName == null) {
          throw new OptionException("registryName has not been set");
       }
       if (registryHost == null) {
          throw new OptionException("registryHost has not been set");
       }
       // set these protocolOptions to defaults if they aren't set
       if (registryPort == null) {
          try {
             addOption(OPTIONS.registryPort, 1099, 0);
          } catch (DuplicateOptionException e) {
          }
       }
       if (runEmbeddedRegistry == null) {
          try {
             addOption(OPTIONS.runEmbeddedRegistry, false, 0);
          } catch (DuplicateOptionException e) {
          }
       }
       // all other protocolOptions are invalid
       if (lease != null) {
          throw new OptionException("lease option in not valid for the rmi protocol");
       }
    }
    */
    final class _Options {
        String name;
        String value;
        int lineNo;
    }
}
