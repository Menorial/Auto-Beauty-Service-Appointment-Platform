-- ============================================
-- 汽车美容服务预约平台 · 数据库结构（DDL）
-- 共 8 张表，仅含建表语句，不含任何业务数据。
--
-- 用法：
--   mysql -u root -p < sql/schema.sql
-- 如需演示数据（管理员 + 员工 + 服务项目）：
--   mysql -u root -p auto_beauty < sql/seed.sql
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `auto_beauty` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `auto_beauty`;

-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `avatar_url` MEDIUMTEXT DEFAULT NULL COMMENT '头像URL',
  `avatar_data` LONGBLOB DEFAULT NULL COMMENT '头像数据',
  `address` VARCHAR(200) DEFAULT NULL COMMENT '地址',
  `points` INT DEFAULT 0 COMMENT '积分',
  `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色：USER-普通用户，ADMIN-管理员，EMPLOYEE-员工',
  `employee_id` BIGINT DEFAULT NULL COMMENT '员工ID，当角色为EMPLOYEE时关联到employees表',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_username` (`username`),
  KEY `idx_email` (`email`),
  KEY `idx_phone` (`phone`),
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. 员工表
-- ============================================
CREATE TABLE IF NOT EXISTS `employees` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '员工ID',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `skills` VARCHAR(200) DEFAULT NULL COMMENT '技能特长',
  `work_schedule` VARCHAR(100) DEFAULT NULL COMMENT '工作时间',
  `avatar_url` MEDIUMTEXT DEFAULT NULL COMMENT '头像URL',
  `avatar_data` LONGBLOB DEFAULT NULL COMMENT '头像数据',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-离职，1-在职',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- ============================================
-- 3. 服务项目表
-- ============================================
CREATE TABLE IF NOT EXISTS `services` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '服务ID',
  `name` VARCHAR(100) NOT NULL COMMENT '服务名称',
  `description` TEXT DEFAULT NULL COMMENT '服务描述',
  `price` DECIMAL(10,2) NOT NULL COMMENT '服务价格',
  `duration` INT DEFAULT NULL COMMENT '服务时长（分钟）',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '服务分类',
  `image_url` MEDIUMTEXT DEFAULT NULL COMMENT '服务图片URL',
  `image_data` LONGBLOB DEFAULT NULL COMMENT '服务图片数据',
  `status` INT DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务项目表';

-- ============================================
-- 4. 车辆表
-- ============================================
CREATE TABLE IF NOT EXISTS `vehicles` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '车辆ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `brand` VARCHAR(50) NOT NULL COMMENT '品牌',
  `model` VARCHAR(50) NOT NULL COMMENT '型号',
  `license_plate` VARCHAR(20) NOT NULL COMMENT '车牌号',
  `vehicle_type` VARCHAR(20) DEFAULT 'FUEL' COMMENT '车辆类型：FUEL-燃油，HEV-混动，PHEV-插混，BEV-纯电',
  `color` VARCHAR(20) DEFAULT NULL COMMENT '颜色',
  `year` INT DEFAULT NULL COMMENT '年份',
  `image_url` MEDIUMTEXT DEFAULT NULL COMMENT '车辆图片URL',
  `image_data` LONGBLOB DEFAULT NULL COMMENT '车辆图片数据',
  `is_default` TINYINT DEFAULT 0 COMMENT '是否默认车辆：0-否，1-是',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_license_plate` (`license_plate`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆表';

-- ============================================
-- 5. 预约表
-- ============================================
CREATE TABLE IF NOT EXISTS `appointments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约ID',
  `order_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `vehicle_id` BIGINT NOT NULL COMMENT '车辆ID',
  `service_id` BIGINT NOT NULL COMMENT '服务ID',
  `employee_id` BIGINT DEFAULT NULL COMMENT '员工ID',
  `appointment_time` DATETIME NOT NULL COMMENT '预约时间',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '预约状态：PENDING-待确认，CONFIRMED-已确认，IN_PROGRESS-进行中，COMPLETED-已完成，CANCELLED-已取消',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',
  `payment_status` VARCHAR(20) DEFAULT 'UNPAID' COMMENT '支付状态：UNPAID-未支付，PAID-已支付，REFUNDED-已退款',
  `payment_method` VARCHAR(20) DEFAULT NULL COMMENT '支付方式',
  `payment_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `notes` TEXT DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_vehicle_id` (`vehicle_id`),
  KEY `idx_service_id` (`service_id`),
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_status` (`status`),
  KEY `idx_payment_status` (`payment_status`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`vehicle_id`) REFERENCES `vehicles`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`service_id`) REFERENCES `services`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`employee_id`) REFERENCES `employees`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- ============================================
-- 6. 评价表
-- ============================================
CREATE TABLE IF NOT EXISTS `reviews` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `appointment_id` BIGINT NOT NULL COMMENT '预约ID',
  `service_id` BIGINT NOT NULL COMMENT '服务ID',
  `rating` INT NOT NULL COMMENT '评分（1-5星）',
  `comment` TEXT DEFAULT NULL COMMENT '评论内容',
  `images` VARCHAR(500) DEFAULT NULL COMMENT '评价图片URL',
  `images_data` LONGBLOB DEFAULT NULL COMMENT '评价图片数据',
  `reply` TEXT DEFAULT NULL COMMENT '商家回复',
  `reply_time` DATETIME DEFAULT NULL COMMENT '回复时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_appointment_id` (`appointment_id`),
  KEY `idx_service_id` (`service_id`),
  KEY `idx_rating` (`rating`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`appointment_id`) REFERENCES `appointments`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`service_id`) REFERENCES `services`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- ============================================
-- 7. 积分记录表
-- ============================================
CREATE TABLE IF NOT EXISTS `point_records` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `type` VARCHAR(20) NOT NULL COMMENT '类型：EARN-获得，SPEND-消费',
  `points` INT NOT NULL COMMENT '积分数量',
  `reason` VARCHAR(100) DEFAULT NULL COMMENT '原因说明',
  `related_id` BIGINT DEFAULT NULL COMMENT '关联ID（如订单ID）',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分记录表';

-- ============================================
-- 8. 系统配置表
-- ============================================
CREATE TABLE IF NOT EXISTS `system_configs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
  `config_value` TEXT DEFAULT NULL COMMENT '配置值',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '配置说明',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';