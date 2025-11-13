CREATE TABLE IF NOT EXISTS `baseentity`
(
    `id`         BIGINT,
    `created_at` DATETIME,
    `updated_at` DATETIME,
    `is_deleted` BOOLEAN,
    `deleted_at` DATETIME
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `refresh_tokens`
(
    `id`           BIGINT,
    `user`         BIGINT(255),
    `device_id`    VARCHAR(255),
    `device_name`  VARCHAR(255),
    `token`        VARCHAR(255),
    `expires_at`   DATETIME,
    `last_used_at` DATETIME,
    `ip_address`   VARCHAR(255),
    `user_agent`   VARCHAR(255)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `categories`
(
    `id`        BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name`      VARCHAR(255) NOT NULL,
    `parent_id` BIGINT,
    UNIQUE KEY uq_category_name_parent (`name`, `parent_id`)
    -- ...
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `chats`
(
    `id`       BIGINT,
    `content`  VARCHAR(255),
    `chatroom` BIGINT(255),
    `user`     BIGINT(255)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `chat_participating_user`
(
    `id`         BIGINT,
    `chatroom`   BIGINT(255),
    `user`       BIGINT(255),
    `deleted_at` DATETIME,
    `is_deleted` BOOLEAN
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `chatparticipatinguserid`
(
    `id`          BIGINT,
    `chatroom_id` BIGINT,
    `user_id`     BIGINT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `chatrooms`
(
    `id`         BIGINT,
    `sale_post`  BIGINT(255),
    `created_at` DATETIME
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `closets`
(
    `id`           BIGINT,
    `name`         VARCHAR(255),
    `description`  VARCHAR(255),
    `is_public`    BOOLEAN,
    `user`         BIGINT(255),
    `closet_image` BIGINT(255)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `closet_clothes_links`
(
    `id`      BIGINT,
    `closet`  BIGINT(255),
    `clothes` BIGINT(255)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `closet_images`
(
    `id`     BIGINT,
    `closet` BIGINT(255),
    `image`  BIGINT(255)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `clothes`
(
    `id`            BIGINT,
    `category`      BIGINT(255),
    `user`          BIGINT(255),
    `clothes_size`  VARCHAR(255),
    `clothes_color` VARCHAR(255),
    `description`   VARCHAR(255),
    `last_worn_at`  DATETIME
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `clothes_images`
(
    `id`      BIGINT,
    `clothes` BIGINT(255),
    `image`   BIGINT(255),
    `is_main` BOOLEAN
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `donation_centers`
(
    `id`              BIGINT,
    `kakao_place_id`  VARCHAR(255),
    `name`            VARCHAR(255),
    `address`         VARCHAR(255),
    `phone_number`    VARCHAR(255),
    `operating_hours` VARCHAR(255),
    `location`        VARCHAR(255),
    `description`     VARCHAR(255)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `images`
(
    `id`           BIGINT,
    `url`          VARCHAR(255),
    `file_name`    VARCHAR(255),
    `s3_key`       VARCHAR(255),
    `content_type` VARCHAR(255),
    `size`         BIGINT,
    `type`         VARCHAR(255)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `payments`
(
    `id`                BIGINT,
    `amount`            VARCHAR(255),
    `method`            VARCHAR(255),
    `status`            VARCHAR(255),
    `toss_order_id`     VARCHAR(255),
    `toss_payment_key`  VARCHAR(255),
    `easy_pay_provider` VARCHAR(255),
    `requested_at`      DATETIME,
    `approved_at`       DATETIME,
    `settled_at`        DATETIME,
    `refunded_at`       DATETIME,
    `refund_type`       VARCHAR(255),
    `refund_reason`     VARCHAR(255),
    `receipt_url`       VARCHAR(255),
    `transaction`       BIGINT(255)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `recommendations`
(
    `id`      BIGINT,
    `clothes` BIGINT(255),
    `user`    BIGINT(255),
    `type`    VARCHAR(255),
    `reason`  VARCHAR(255),
    `status`  VARCHAR(255)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `recommendation_batch_history`
(
    `id`                    BIGINT,
    `start_time`            DATETIME,
    `end_time`              DATETIME,
    `status`                VARCHAR(255),
    `total_users`           INT,
    `success_users`         INT,
    `failed_users`          INT,
    `total_recommendations` INT,
    `execution_time_ms`     BIGINT,
    `error_message`         VARCHAR(255)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `sale_posts`
(
    `id`             BIGINT,
    `title`          VARCHAR(255),
    `content`        VARCHAR(255),
    `price`          VARCHAR(255),
    `status`         VARCHAR(255),
    `trade_address`  VARCHAR(255),
    `trade_location` VARCHAR(255),
    `user`           BIGINT(255),
    `category`       BIGINT(255),
    `recommendation` BIGINT(255)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `sale_post_images`
(
    `id`            BIGINT,
    `image_url`     VARCHAR(255),
    `display_order` INT,
    `sale_post`     BIGINT(255)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `transactions`
(
    `id`                  BIGINT,
    `price`               VARCHAR(255),
    `status`              VARCHAR(255),
    `approved_at`         DATETIME,
    `confirmed_at`        DATETIME,
    `cancel_requested_at` DATETIME,
    `cancel_reason`       VARCHAR(255),
    `buyer`               BIGINT(255),
    `sale_post`           BIGINT(255),
    `chat_room`           VARCHAR(255),
    `payment`             BIGINT(255)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `users`
(
    `id`              BIGINT,
    `login_id`        VARCHAR(255),
    `email`           VARCHAR(255),
    `nickname`        VARCHAR(255),
    `username`        VARCHAR(255),
    `password`        VARCHAR(255),
    `phone_number`    VARCHAR(255),
    `role`            VARCHAR(255),
    `trade_address`   VARCHAR(255),
    `trade_location`  VARCHAR(255),
    `image_url`       VARCHAR(255),
    `user_image`      BIGINT(255),
    `login_type`      VARCHAR(255),
    `social_provider` VARCHAR(255),
    `social_id`       VARCHAR(255)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `user_images`
(
    `id`    BIGINT,
    `image` BIGINT(255)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `wear_records`
(
    `id`      BIGINT,
    `user`    BIGINT(255),
    `clothes` BIGINT(255),
    `worn_at` DATETIME
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;