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
import com.mcinfotech.event.handler.config.EventHandlerRuleConfig;
import com.mcinfotech.event.handler.domain.EventHandlerRule;

@Configuration
public class RuleDefineCacheConfig {
	@Resource
	EventHandlerRuleConfig eventHandlerRulesConfig;
	@Resource
	ProjectInfo projectInfo;

	@Bean
	public AsyncLoadingCache<String, EventHandlerRule> buildRuleDefineAsyncCache() {
		return Caffeine.newBuilder().maximumSize(10_000).expireAfterWrite(30, TimeUnit.MINUTES)
				.buildAsync(new CacheLoader<String, EventHandlerRule>() {
					@Override
					public @Nullable EventHandlerRule load(@NonNull String key) throws Exception {
						//int columnDefineId = Integer.valueOf(StringUtils.substringBefore(key, "_"));
						return eventHandlerRulesConfig.getEventHandlerRule(projectInfo.getId(), key,null,null);
					}

					@Override
					public @NonNull Map<@NonNull String, @NonNull EventHandlerRule> loadAll(
							@NonNull Iterable<? extends @NonNull String> keys) throws Exception {
						Map<String, EventHandlerRule> defineMap = new HashMap<>();
						Map<String, Object> conditions = new HashMap<String, Object>();
						conditions.put("isEnable", "Y");
						eventHandlerRulesConfig.getEventHandlerRules(projectInfo.getId(), conditions).forEach(define -> {
							defineMap.put(define.getProjectId() + "_" + define.getId(), define);
						});
						return defineMap;
					}
				});
	}
	
	@Bean
	public LoadingCache<String, EventHandlerRule> buildRuleDefineCache() {
		return Caffeine.newBuilder().maximumSize(10000).expireAfterWrite(4, TimeUnit.HOURS)
				.build(new CacheLoader<String, EventHandlerRule>() {
					@Override
					public @Nullable EventHandlerRule load(@NonNull String key) throws Exception {
						//int columnDefineId = Integer.valueOf(StringUtils.substringBefore(key, "_"));
						return eventHandlerRulesConfig.getEventHandlerRule(projectInfo.getId(), key,null,null);
					}
					@Override
					public @NonNull Map<@NonNull String, @NonNull EventHandlerRule> loadAll(
							@NonNull Iterable<? extends @NonNull String> keys) throws Exception {
						Map<String, EventHandlerRule> defineMap = new HashMap<>();
						Map<String, Object> conditions = new HashMap<String, Object>();
						conditions.put("isEnable", "Y");
						eventHandlerRulesConfig.getEventHandlerRules(projectInfo.getId(), conditions).forEach(define -> {
							defineMap.put(define.getProjectId() + "_" + define.getId(), define);
						});
						return defineMap;
					}
				});
	}
}
