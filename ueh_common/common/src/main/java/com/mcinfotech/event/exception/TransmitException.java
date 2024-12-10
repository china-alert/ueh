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
public class TransmitException extends McException{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7313408882833974674L;

	public TransmitException() {
		super();
	}
	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public TransmitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	/**
	 * @param message
	 * @param cause
	 */
	public TransmitException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param message
	 */
	public TransmitException(String message) {
		super(message);
	}
	/**
	 * @param cause
	 */
	public TransmitException(Throwable cause) {
		super(cause);
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
