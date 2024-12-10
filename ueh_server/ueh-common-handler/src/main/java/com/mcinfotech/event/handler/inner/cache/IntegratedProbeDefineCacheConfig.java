package com.mcinfotech.event.handler.inner.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.mcinfotech.event.domain.ProjectInfo;
import com.mcinfotech.event.handler.config.EventIntegratedProbeConfig;
import com.mcinfotech.event.handler.domain.EventHandlerRule;
import com.mcinfotech.event.handler.domain.EventIntegratedProbe;

@Configuration
public class IntegratedProbeDefineCacheConfig {
	@Resource
	EventIntegratedProbeConfig probeConfig;
	@Resource
	ProjectInfo projectInfo;

	/*
	 * @Bean public AsyncLoadingCache<String, EventIntegratedProbe>
	 * buildIntegragedProbeDefineAsyncCache() { return
	 * Caffeine.newBuilder().maximumSize(10_000).expireAfterWrite(4, TimeUnit.HOURS)
	 * .buildAsync(new CacheLoader<String, EventIntegratedProbe>() {
	 * 
	 * @Override public @Nullable EventIntegratedProbe load(@NonNull String key)
	 * throws Exception { return
	 * probeConfig.getEventIntegratedProbe(projectInfo.getId(), key); }
	 * 
	 * @Override public @NonNull Map<@NonNull String, @NonNull EventIntegratedProbe>
	 * loadAll(@NonNull Iterable<? extends @NonNull String> keys) throws Exception {
	 * Map<String, EventIntegratedProbe> defineMap = new HashMap<>(); Map<String,
	 * Object> conditions = new HashMap<String, Object>();
	 * conditions.put("isEnable", "Y");
	 * probeConfig.getEventIntegratedProbes(projectInfo.getId(),
	 * conditions).forEach(define -> { defineMap.put(define.getProbeKey(), define);
	 * }); return defineMap; } }); }
	 */
	/**
	 * 只构建接入启用的Probe信息
	 * @return
	 */
	@Bean
	public LoadingCache<String, EventIntegratedProbe> buildIntegragedProbeDefineCache() {
		return Caffeine.newBuilder().maximumSize(10000).expireAfterWrite(4, TimeUnit.HOURS)
				.build(new CacheLoader<String, EventIntegratedProbe>() {
					@Override
					public @Nullable EventIntegratedProbe load(@NonNull String key) throws Exception {
						return probeConfig.getEventIntegratedProbe(projectInfo.getId(), key);
					}

					@Override
					public @NonNull Map<@NonNull String, @NonNull EventIntegratedProbe> loadAll(@NonNull Iterable<? extends @NonNull String> keys) throws Exception {
						Map<String, EventIntegratedProbe> defineMap = new HashMap<>();
						Map<String, Object> conditions = new HashMap<String, Object>();
						conditions.put("isEnable", "Y");
						probeConfig.getEventIntegratedProbes(projectInfo.getId(), conditions).forEach(define -> {
							defineMap.put(define.getProbeKey(), define);
						});
						return defineMap;
					}
				});
	}
}
