/**
 * <p>title: JsonException.java</p>
 * <p>description : </p>

 * @date 2020年10月21日
 *
 */
package com.mcinfotech.event.exception;

/**

 *
 */
public class DispatcherException extends McException{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1545726823198822255L;
	/*private String errorCode;
	private String errorMsg;*/
	
	public DispatcherException() {
		super();
	}
	/*public TransmitException(String errorCode,String errorMsg){
		this.errorCode=errorCode;
		this.errorMsg=errorMsg;
	}*/
	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public DispatcherException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param message
	 * @param cause
	 */
	public DispatcherException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param message
	 */
	public DispatcherException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param cause
	 */
	public DispatcherException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
	
	/*private String errorCode="JSON";
	public T JsonException(Enum<T> code,String message) {
		super(((T)code).getComment().concat("-").concat(message));
		this.errorCode=this.errorCode.concat("-").concat(new Integer(((T)code).getValue()).toString());
	}
	public JsonException(Enum code) {
		super(((JSONError)code).getComment());
		this.errorCode=this.errorCode.concat("-").concat(new Integer(((JSONError)code).getValue()).toString());
	}*/
	
}
