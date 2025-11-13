-- users (먼저 생성 - 다른 테이블들이 참조)
CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `login_id` VARCHAR(25) NOT NULL UNIQUE,
  `email` VARCHAR(60) NOT NULL UNIQUE,
  `nickname` VARCHAR(20) NOT NULL UNIQUE,
  `username` VARCHAR(60) NOT NULL,
  `password` VARCHAR(255),
  `phone_number` VARCHAR(30) UNIQUE,
  `role` ENUM('ROLE_ADMIN', 'ROLE_USER') NOT NULL,
  `trade_address` VARCHAR(50) NOT NULL,
  `trade_location` POINT,
  `image_url` VARCHAR(500),
  `user_image_id` BIGINT,
  `login_type` ENUM('LOGIN_ID', 'SOCIAL') NOT NULL,
  `social_provider` ENUM('GOOGLE'),
  `social_id` VARCHAR(100) UNIQUE,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `deleted_at` DATETIME(6),
  `is_deleted` BIT(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- images
CREATE TABLE IF NOT EXISTS `images` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `url` VARCHAR(500) NOT NULL,
  `file_name` VARCHAR(255) NOT NULL,
  `s3key` VARCHAR(1000) NOT NULL,
  `content_type` VARCHAR(100) NOT NULL,
  `size` BIGINT NOT NULL,
  `type` ENUM('CLOSET', 'CLOTHES', 'SALEPOST', 'USER') NOT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `deleted_at` DATETIME(6),
  `is_deleted` BIT(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- user_images
CREATE TABLE IF NOT EXISTS `user_images` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `image_id` BIGINT NOT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `deleted_at` DATETIME(6),
  `is_deleted` BIT(1) DEFAULT 0,
  FOREIGN KEY (`image_id`) REFERENCES images(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- categories
CREATE TABLE IF NOT EXISTS `categories` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(30) NOT NULL,
  `parent_id` BIGINT,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `deleted_at` DATETIME(6),
  `is_deleted` BIT(1) DEFAULT 0,
  FOREIGN KEY (`parent_id`) REFERENCES categories(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `clothes` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `category_id` BIGINT,
  `clothes_size` ENUM('FREE', 'L', 'M', 'S', 'XL', 'XS', 'XXL', 'XXXL'),
  `clothes_color` ENUM('BEIGE', 'BLACK', 'BLUE', 'GRAY', 'GREEN', 'NAVY', 'PINK', 'RED', 'WHITE', 'YELLOW'),
  `description` VARCHAR(255) NOT NULL,
  `last_worn_at` DATETIME(6),
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `deleted_at` DATETIME(6),
  `is_deleted` BIT(1) DEFAULT 0,
  FOREIGN KEY (`category_id`) REFERENCES categories(`id`),
  FOREIGN KEY (`user_id`) REFERENCES users(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- clothes_images
CREATE TABLE IF NOT EXISTS `clothes_images` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `clothes_id` BIGINT NOT NULL,
  `image_id` BIGINT NOT NULL,
  `is_main` BIT(1) DEFAULT 0,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `deleted_at` DATETIME(6),
  `is_deleted` BIT(1) DEFAULT 0,
  FOREIGN KEY (`clothes_id`) REFERENCES clothes(`id`),
  FOREIGN KEY (`image_id`) REFERENCES images(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- closets
CREATE TABLE IF NOT EXISTS `closets` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `description` VARCHAR(255),
  `is_public` BIT(1) NOT NULL DEFAULT 0,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `deleted_at` DATETIME(6),
  `is_deleted` BIT(1) DEFAULT 0,
  FOREIGN KEY (`user_id`) REFERENCES users(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- closet_clothes_links
CREATE TABLE IF NOT EXISTS `closet_clothes_links` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `closet_id` BIGINT NOT NULL,
  `clothes_id` BIGINT NOT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `deleted_at` DATETIME(6),
  `is_deleted` BIT(1) DEFAULT 0,
  FOREIGN KEY (`closet_id`) REFERENCES closets(`id`),
  FOREIGN KEY (`clothes_id`) REFERENCES clothes(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- closet_images
CREATE TABLE IF NOT EXISTS `closet_images` (
  `closet_id` BIGINT PRIMARY KEY,
  `image_id` BIGINT NOT NULL UNIQUE,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `deleted_at` DATETIME(6),
  `is_deleted` BIT(1) DEFAULT 0,
  FOREIGN KEY (`closet_id`) REFERENCES closets(`id`),
  FOREIGN KEY (`image_id`) REFERENCES images(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- sale_posts
CREATE TABLE IF NOT EXISTS `sale_posts` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `category_id` BIGINT NOT NULL,
  `recommendation_id` BIGINT,
  `title` VARCHAR(100) NOT NULL,
  `content` TEXT NOT NULL,
  `price` DECIMAL(10,0) NOT NULL,
  `status` ENUM('AVAILABLE', 'CANCELLED', 'COMPLETED', 'DELETED', 'RESERVED', 'TRADING') NOT NULL,
  `trade_address` VARCHAR(50) NOT NULL,
  `trade_location` POINT NOT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `deleted_at` DATETIME(6),
  `is_deleted` BIT(1) DEFAULT 0,
  FOREIGN KEY (`category_id`) REFERENCES categories(`id`),
  FOREIGN KEY (`user_id`) REFERENCES users(`id`),
  FOREIGN KEY (`recommendation_id`) REFERENCES recommendations(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- sale_post_images
CREATE TABLE IF NOT EXISTS `sale_post_images` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `sale_post_id` BIGINT NOT NULL,
  `image_url` VARCHAR(500) NOT NULL,
  `display_order` INT NOT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `deleted_at` DATETIME(6),
  `is_deleted` BIT(1) DEFAULT 0,
  FOREIGN KEY (`sale_post_id`) REFERENCES sale_posts(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- chatrooms
CREATE TABLE IF NOT EXISTS `chatrooms` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `sale_post_id` BIGINT NOT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `deleted_at` DATETIME(6),
  `is_deleted` BIT(1) DEFAULT 0,
  FOREIGN KEY (`sale_post_id`) REFERENCES sale_posts(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- chat_participating_user
CREATE TABLE IF NOT EXISTS `chat_participating_user` (
  `chatroom_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `deleted_at` DATETIME(6),
  `is_deleted` BIT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`chatroom_id`, `user_id`),
  FOREIGN KEY (`chatroom_id`) REFERENCES chatrooms(`id`),
  FOREIGN KEY (`user_id`) REFERENCES users(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- chats
CREATE TABLE IF NOT EXISTS `chats` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `content` TEXT NOT NULL,
  `chatroom_id` BIGINT,
  `user_id` BIGINT,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `deleted_at` DATETIME(6),
  `is_deleted` BIT(1) DEFAULT 0,
  FOREIGN KEY (`chatroom_id`) REFERENCES chatrooms(`id`),
  FOREIGN KEY (`user_id`) REFERENCES users(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- transactions
CREATE TABLE IF NOT EXISTS `transactions` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `buyer_id` BIGINT NOT NULL,
  `sale_post_id` BIGINT NOT NULL,
  `chat_room_id` BIGINT NOT NULL,
  `price` DECIMAL(12,0) NOT NULL,
  `status` ENUM('PENDING_APPROVAL', 'APPROVED', 'CONFIRMED', 'CANCELLED_BY_BUYER', 'CANCELLED_BY_SELLER', 'PAYMENT_FAILED', 'EXPIRED') NOT NULL,
  `approved_at` DATETIME(6),
  `confirmed_at` DATETIME(6),
  `cancel_requested_at` DATETIME(6),
  `cancel_reason` VARCHAR(500),
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `deleted_at` DATETIME(6),
  `is_deleted` BIT(1) DEFAULT 0,
  FOREIGN KEY (`buyer_id`) REFERENCES users(`id`),
  FOREIGN KEY (`chat_room_id`) REFERENCES chatrooms(`id`),
  FOREIGN KEY (`sale_post_id`) REFERENCES sale_posts(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- payments
CREATE TABLE IF NOT EXISTS `payments` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `transaction_id` BIGINT NOT NULL UNIQUE,
  `toss_payment_key` VARCHAR(200),
  `toss_order_id` VARCHAR(36) NOT NULL UNIQUE,
  `method` ENUM('ACCOUNT_TRANSFER', 'EASY_PAY') NOT NULL,
  `easy_pay_provider` ENUM('NAVER_PAY', 'TOSS_PAY'),
  `amount` DECIMAL(12,0) NOT NULL,
  `status` ENUM('PENDING', 'ESCROWED', 'SETTLED', 'REFUNDED', 'FAILED') NOT NULL,
  `requested_at` DATETIME(6),
  `approved_at` DATETIME(6),
  `settled_at` DATETIME(6),
  `refunded_at` DATETIME(6),
  `refunded_amount` DECIMAL(12,0) DEFAULT 0,
  `refund_type` ENUM('BUYER_CANCELLED', 'SELLER_CANCELLED', 'MUTUAL_AGREEMENT', 'PRODUCT_UNAVAILABLE', 'TIMEOUT', 'ETC'),
  `refund_reason` VARCHAR(500),
  `receipt_url` VARCHAR(500),
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `deleted_at` DATETIME(6),
  `is_deleted` BIT(1) DEFAULT 0,
  FOREIGN KEY (`transaction_id`) REFERENCES transactions(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- recommendations
CREATE TABLE IF NOT EXISTS `recommendations` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `clothes_id` BIGINT NOT NULL,
  `type` ENUM('DONATION', 'SALE') NOT NULL,
  `reason` VARCHAR(255) NOT NULL,
  `status` ENUM('PENDING', 'ACCEPTED', 'REJECTED') NOT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `deleted_at` DATETIME(6),
  `is_deleted` BIT(1) DEFAULT 0,
  FOREIGN KEY (`clothes_id`) REFERENCES clothes(`id`),
  FOREIGN KEY (`user_id`) REFERENCES users(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- donation_centers
CREATE TABLE IF NOT EXISTS `donation_centers` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `kakao_place_id` VARCHAR(50) NOT NULL UNIQUE,
  `name` VARCHAR(100) NOT NULL,
  `address` VARCHAR(255) NOT NULL,
  `phone_number` VARCHAR(20),
  `operating_hours` VARCHAR(100),
  `location` POINT NOT NULL SRID 4326,
  `description` VARCHAR(255),
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `deleted_at` DATETIME(6),
  `is_deleted` BIT(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- wear_records
CREATE TABLE IF NOT EXISTS `wear_records` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `clothes_id` BIGINT NOT NULL,
  `worn_at` DATETIME(6) NOT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `deleted_at` DATETIME(6),
  `is_deleted` BIT(1) DEFAULT 0,
  FOREIGN KEY (`clothes_id`) REFERENCES clothes(`id`),
  FOREIGN KEY (`user_id`) REFERENCES users(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- refresh_tokens
CREATE TABLE IF NOT EXISTS `refresh_tokens` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `device_id` VARCHAR(255) NOT NULL,
  `device_name` VARCHAR(100),
  `token` VARCHAR(500) NOT NULL UNIQUE,
  `expires_at` DATETIME(6) NOT NULL,
  `last_used_at` DATETIME(6),
  `ip_address` VARCHAR(45),
  `user_agent` VARCHAR(500),
  UNIQUE KEY `uk_user_device` (`user_id`, `device_id`),
  FOREIGN KEY (`user_id`) REFERENCES users(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
