package com.mcinfotech.event.probe.config;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver;
import org.springframework.web.method.annotation.RequestHeaderMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

@Component
public class ReplaceRequestHeaderMethodArgumentResolver extends AbstractNamedValueMethodArgumentResolver {

	@Autowired
	private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

	@Autowired
	private DefaultListableBeanFactory defaultListableBeanFactory;

	public ReplaceRequestHeaderMethodArgumentResolver(ConfigurableBeanFactory beanFactory) {
		super(beanFactory);
	}

	@PostConstruct
	public void init() {

		List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>(
				requestMappingHandlerAdapter.getArgumentResolvers());
		for (int i = 0; i < resolvers.size(); i++) {
			if (resolvers.get(i) instanceof RequestHeaderMethodArgumentResolver) {
				resolvers.set(i, new ReplaceRequestHeaderMethodArgumentResolver(defaultListableBeanFactory));
			}
		}
		requestMappingHandlerAdapter.setArgumentResolvers(resolvers);
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return (parameter.hasParameterAnnotation(RequestHeader.class)
				&& !Map.class.isAssignableFrom(parameter.nestedIfOptional().getNestedParameterType()));
	}

	@Override
	protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
		RequestHeader ann = parameter.getParameterAnnotation(RequestHeader.class);
		Assert.state(ann != null, "No RequestHeader annotation");
		return new ReplaceRequestHeaderMethodArgumentResolver.RequestHeaderNamedValueInfo(ann);
	}

	@Override
	@Nullable
	protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) throws Exception {
		String[] headerValues = request.getHeaderValues(name);

		if (headerValues == null) {
			return null;
		}

		if (parameter.getParameterType() != String.class) {
			String objStr = headerValues[0];
			if (StringUtils.isEmpty(objStr)) {
				return null;
			}
			//String decode = URLDecoder.decode(objStr, "UTF-8");
			//return this.objectMapper.readValue(decode, parameter.getParameterType());
			return objStr;
		}

		return (headerValues.length == 1 ? headerValues[0] : headerValues);
	}

	@Override
	protected void handleMissingValue(String name, MethodParameter parameter) throws ServletRequestBindingException {
		throw new MissingRequestHeaderException(name, parameter);
	}

	private static final class RequestHeaderNamedValueInfo extends NamedValueInfo {

		private RequestHeaderNamedValueInfo(RequestHeader annotation) {
			super(annotation.name(), annotation.required(), annotation.defaultValue());
		}
	}
}
