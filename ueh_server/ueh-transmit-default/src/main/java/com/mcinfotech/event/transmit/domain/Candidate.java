package com.mcinfotech.event.transmit.domain;

/**
 * 事件通知对象，候选人

 *
 */
public class Candidate {
	private String username;
	private String deptName;
	private String email;
	private String phonenumber;
	private String wechat;
	private String nailed;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getDeptName() {
		return deptName;
	}
	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhonenumber() {
		return phonenumber;
	}
	public void setPhonenumber(String phonenumber) {
		this.phonenumber = phonenumber;
	}
	public String getWechat() {
		return wechat;
	}
	public void setWechat(String wechat) {
		this.wechat = wechat;
	}
	public String getNailed() {
		return nailed;
	}
	public void setNailed(String nailed) {
		this.nailed = nailed;
	}
	@Override
	public String toString() {
		return "Candidate [username=" + username + ", deptName=" + deptName + ", email=" + email + ", phonenumber="
				+ phonenumber + ", wechat=" + wechat + ", nailed=" + nailed + "]";
	}
}
