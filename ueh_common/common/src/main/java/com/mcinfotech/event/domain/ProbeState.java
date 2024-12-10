package com.mcinfotech.event.domain;

import java.sql.Timestamp;

/**
 * 组件运行情况

 *
 */
public class ProbeState {
	/**
	 * 组件名称
	 */
	private String probeName;
	/**
	 * 组件Key
	 */
	private String probeKey;
	/**
	 * 组件当前运行状态
	 */
	private ProbeStateType currentState;
	/**
	 * 上次运行状态
	 */
	private ProbeStateType lastState;
	/**
	 * 上报时间戳
	 */
	private String reported;
	/**
	 * 上次上报时间戳
	 */
	private Timestamp lastReported;
	/**
	 * 运行主机
	 */
	private String host;
	private String className;
	private String methodName;
	private String errorMsg;
	
	public String getProbeName() {
		return probeName;
	}
	public void setProbeName(String probeName) {
		this.probeName = probeName;
	}
	public String getProbeKey() {
		return probeKey;
	}
	public void setProbeKey(String probeKey) {
		this.probeKey = probeKey;
	}
	public ProbeStateType getCurrentState() {
		return currentState;
	}
	public void setCurrentState(ProbeStateType currentState) {
		this.currentState = currentState;
	}
	public ProbeStateType getLastState() {
		return lastState;
	}
	public void setLastState(ProbeStateType lastState) {
		this.lastState = lastState;
	}
	/*public Timestamp getReported() {
		return reported;
	}
	public void setReported(Timestamp reported) {
		this.reported = reported;
	}*/
	public Timestamp getLastReported() {
		return lastReported;
	}
	public String getReported() {
		return reported;
	}
	public void setReported(String reported) {
		this.reported = reported;
	}
	public void setLastReported(Timestamp lastReported) {
		this.lastReported = lastReported;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
}
