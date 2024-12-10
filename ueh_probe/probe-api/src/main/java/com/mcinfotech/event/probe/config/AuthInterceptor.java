package com.mcinfotech.event.probe.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.mcinfotech.event.utils.FastJsonUtils;

@Component
public class AuthInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		/*
		 * log.info(">>>AuthInterceptor>>>>>>>在请求处理之前进行调用（Controller方法调用之前)");
		 * 
		 * String token = request.getHeader("token");
		 * 
		 * log.info("token : [ {} ]", token);
		 * 
		 * // ....处理逻辑
		 */
		//System.out.println(FastJsonUtils.convertObjectToJSON(handler));
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			System.out.println("header name [" + headerName + "], value is " + request.getHeader(headerName));
		}
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String paramName = parameterNames.nextElement();
			System.out.println("param name [" + paramName + "], value is " + request.getParameter(paramName));
		}
		// request.getAttributeNames();
		Enumeration<String> attrNames = request.getAttributeNames();
		while (attrNames.hasMoreElements()) {
			String attrName = attrNames.nextElement();
			System.out.println("attr name [" + attrName + "], value is " + request.getAttribute(attrName));
		}
		/*
		 * InputStream is = null; StringBuilder sb = new StringBuilder(); try {
		 * is = request.getInputStream();
		 * 
		 * byte[] b = new byte[4096]; for (int n; (n = is.read(b)) != -1;) {
		 * sb.append(new String(b, 0, n)); }
		 * 
		 * } catch (IOException e) { e.printStackTrace(); } finally { if (null
		 * != is) { try { is.close(); } catch (IOException e) {
		 * e.printStackTrace(); } } System.out.println(sb.toString()); }
		 */
		/*BufferedReader br = null;
		StringBuilder sb = new StringBuilder("");
		try {
			br = request.getReader();
			String str;
			while ((str = br.readLine()) != null) {
				sb.append(str);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != br) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println(sb.toString());*/
		return true;// 只有返回true才会继续向下执行，返回false取消当前请求
	}

	/*@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub
		//HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
		System.out.println("post");
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder("");
		try {
			br = request.getReader();
			String str;
			while ((str = br.readLine()) != null) {
				sb.append(str);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != br) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println(sb.toString());
	}*/

	/*@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		//HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
		System.out.println("after");
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder("");
		try {
			br = request.getReader();
			String str;
			while ((str = br.readLine()) != null) {
				sb.append(str);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != br) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println(sb.toString());
	}*/
}
