package com.mcinfotech.event.inner;

import java.util.List;

/**

 */
public class Consumer<T> {
	private List<T> consumerList;

	public Consumer(List<T> consumerList) {
		this.consumerList = consumerList;
	}

	public T get(int index) {
		return consumerList.get(index);
	}
}
