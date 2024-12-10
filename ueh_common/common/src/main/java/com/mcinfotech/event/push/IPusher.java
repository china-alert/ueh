package com.mcinfotech.event.push;

import java.util.List;

/**

 */
public interface IPusher<T> {
	void push(T message);
	void push(List<T> messages);
	void push(String handlerType,T messages);
	void push(String handlerType,List<T> messages);
}
