-- 共享积分增加
ALTER TABLE r_points_operation_log
    ADD COLUMN oper_user_id BIGINT COMMENT '操作人员' AFTER user_id;