package org.remotej.ddl.trees;

/**
 * Copyright(c) Paul Soule. All rights reserved.
 * <p/>
 * This file is part of the RemoteJ system.
 * <p/>
 * Created at 4:38:59 PM on Mar 5, 2006
 */
public final class Parameter {

   public Identifier type;
   public String referenceType = null;
   public Identifier value;
   public enum CALL_TYPE {REFERENCE, COPY, RESTORE}; 
   public CALL_TYPE callType;
   public Object generatedInterface = null;
   
   public Parameter(CALL_TYPE callType, Identifier type, Identifier value) {
      this.callType = callType;
      this.type  = type;
      this.value = value;
   }

   public Parameter(Identifier value) {
      this.type  = null;
      this.value = value;
   }
}
