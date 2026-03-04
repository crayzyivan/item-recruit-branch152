-- 将默认subreddit修改成test 原recruit不支持发连接
update r_ayrshare_company_config set ayrshare_subreddit = 'test' where id in (1, 2);