package com.hps.mayo.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;

public class MdmProcessingException extends RuntimeException {

	private static final long serialVersionUID = 5580950428867429165L;
	private String errorCode = "MAYO-0000";
	private String rowId = "";
	private String pkey = "";
	private String sourceSystem = "";

	public MdmProcessingException() {
		super();
	}

	public MdmProcessingException(String code, Throwable cause, String pkey, String sourceSystem) {
		super(cause);
		this.errorCode = code;
		this.pkey = pkey;
		this.sourceSystem = sourceSystem;
	}

	public MdmProcessingException(Throwable cause) {
		super(cause);
	}

	public MdmProcessingException(String msg) {
		super(msg);
	}

	public MdmProcessingException(String msg, String pkey, String sourceSystem) {
		super(msg);
		this.pkey = pkey;
		this.sourceSystem = sourceSystem;
	}

	public MdmProcessingException(String code, String msg, String pkey, String sourceSystem) {
		super(msg);
		this.errorCode = code;
		this.pkey = pkey;
		this.sourceSystem = sourceSystem;
	}

	public MdmProcessingException(String code, String msg) {
		super(msg);
		this.errorCode = code;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getRowId() {
		return rowId;
	}

	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getPkey() {
		return pkey;
	}

	public void setPkey(String pkey) {
		this.pkey = pkey;
	}

	public String getSourceSystem() {
		return sourceSystem;
	}

	public void setSourceSystem(String sourceSystem) {
		this.sourceSystem = sourceSystem;
	}

	public String getInternalErrorMessage() {
		Throwable cause = this.getCause();
		if (cause != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			cause.printStackTrace(pw);
			return sw.toString();
		}
		return null;
	}
}