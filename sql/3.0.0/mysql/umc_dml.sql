INSERT INTO umc.t_view_page_dataset (name, ds_group, label_text, dataset_type, columns, label_texts, data_types, return_type, exec_type, exec_sql, filter_param_names, filter_values, main_datasource_name, union_dataset_names, union_condition, is_batch, batch_setting, is_enable, project_id, remark, lm_timestamp, union_filter_values) VALUES ('getCandidateByProblemId', '事件通知程序', '根据告警事件ID查询是否自动通知', 'S', 'candidate', '通知人', 'string', 'MRSC', 'QUERY', 'select candidate from t_event_notification_log where "event_id"=''${eventId}''', null, null, 'event_data', null, null, 'N', null, 'Y', 10, null, '2025-01-21 10:44:55.000000', null);