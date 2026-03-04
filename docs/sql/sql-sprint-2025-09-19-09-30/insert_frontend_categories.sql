-- Insert frontend unique and low-match categories into r_job_category table
-- Starting from ID 51 as requested
-- Based on category mapping analysis for RP-245

INSERT INTO r_job_category (id, category_name) VALUES
-- 完全独有的categories (5个)
(51, 'AI Automation'),
(52, 'Content Creation'),
(53, 'Human Resources'),
(54, 'Freelance and Contract Work'),
(55, 'Engineering'),

-- 匹配度不足需要补充的categories (4个)
(56, 'Business and Finance'),
(57, 'Marketing and Sales'),
(58, 'Retail and Customer Service'),
(59, 'Scientific Research');

-- Total: 9 new categories added (IDs 51-59)
-- These categories support frontend landing page requirements
-- and fill gaps in backend category coverage
