package com.mcinfotech.event.handler.inner.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.mcinfotech.event.domain.ProjectInfo;
import com.mcinfotech.event.handler.config.PlatformEventColumnConfig;

import cn.mcinfotech.data.service.db.ColumnDefine;

@Configuration
public class ColumnDefineCacheConfig {
	@Resource
	PlatformEventColumnConfig columnDefineConfig;
	@Resource
	ProjectInfo projectInfo;

	/*
	 * @Bean AsyncLoadingCache<String, ColumnDefine> buildColumnDefineAsyncCache() {
	 * return Caffeine.newBuilder().maximumSize(10_000).expireAfterWrite(4,
	 * TimeUnit.HOURS) .buildAsync(new CacheLoader<String, ColumnDefine>() {
	 * 
	 * @Override public @Nullable ColumnDefine load(@NonNull String key) throws
	 * Exception { //int columnDefineId =
	 * Integer.valueOf(StringUtils.substringBefore(key, "_")); return
	 * columnDefineConfig.getPlatformEventColumn(projectInfo.getId(), key); }
	 * 
	 * @Override public @NonNull Map<@NonNull String, @NonNull ColumnDefine>
	 * loadAll(
	 * 
	 * @NonNull Iterable<? extends @NonNull String> keys) throws Exception {
	 * Map<String, ColumnDefine> columnDefineMap = new HashMap<>(); Map<String,
	 * Object> conditions = new HashMap<String, Object>();
	 * conditions.put("isEnable", "Y");
	 * columnDefineConfig.getPlatformEventColumn(projectInfo.getId(),
	 * conditions).forEach(define -> { columnDefineMap.put(define.getProjectId() +
	 * "_" + define.getId(), define); }); return columnDefineMap; } }); }
	 */

	@Bean
	LoadingCache<String, ColumnDefine> buildColumnDefineCache() {
		return Caffeine.newBuilder().maximumSize(10_000).expireAfterWrite(4, TimeUnit.HOURS)
				.build(new CacheLoader<String, ColumnDefine>() {
					@Override
					public @Nullable ColumnDefine load(@NonNull String key) throws Exception {
						//int columnDefineId = Integer.valueOf(StringUtils.substringBefore(key, "_"));
						return columnDefineConfig.getPlatformEventColumn(projectInfo.getId(), key);
					}
					@Override
					public @NonNull Map<@NonNull String, @NonNull ColumnDefine> loadAll(@NonNull Iterable<? extends @NonNull String> keys) throws Exception {
						Map<String, ColumnDefine> columnDefineMap = new HashMap<>();
						Map<String, Object> conditions = new HashMap<String, Object>();
						conditions.put("isEnable", "Y");
						conditions.put("columnTypeModify", "columnTypeModify");
						boolean isAll=false;
						for(String key:keys) {
							if(key.equalsIgnoreCase("ALL")) {
								isAll=true;
								break;
							}
						}
						if(!isAll) {
							conditions.put("columnInDbs", keys);
						}
						columnDefineConfig.getPlatformEventColumn(projectInfo.getId(), conditions).forEach(define -> {
							columnDefineMap.put(define.getColumnInDB(), define);
						});
						return columnDefineMap;
					}
				});
	}
}
