package com.mcinfotech.event.dispatcher.domain;

public class DispatcherInfo {
	private int batch;
	private int interval;
	private boolean timerEnable;
	
	public int getBatch() {
		return batch;
	}
	public void setBatch(int batch) {
		this.batch = batch;
	}
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
	public boolean isTimerEnable() {
		return timerEnable;
	}
	public void setTimerEnable(boolean timerEnable) {
		this.timerEnable = timerEnable;
	}
	@Override
	public String toString() {
		return "DispatcherInfo [batch=" + batch + ", interval=" + interval + ", timerEnable=" + timerEnable + "]";
	}
}
