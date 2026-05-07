-- MVP schema for barber booking.
-- This file is applied only when the MySQL Docker volume is first initialized.
-- For an existing local volume, apply it manually with:
-- docker compose exec -T mysql mysql -u$MYSQL_USERNAME -p$MYSQL_PASSWORD $MYSQL_DATABASE < docker/mysql/init/002-mvp-schema.sql

SET NAMES utf8mb4;
SET time_zone = '+08:00';

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid VARCHAR(128) NOT NULL,
    nickname VARCHAR(100) NULL,
    avatar_url VARCHAR(500) NULL,
    phone VARCHAR(30) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_users_openid (openid),
    KEY idx_users_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS shops (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    address VARCHAR(255) NULL,
    phone VARCHAR(30) NULL,
    description TEXT NULL,
    opening_time TIME NULL,
    closing_time TIME NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    KEY idx_shops_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS services (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    shop_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    description TEXT NULL,
    price_cents INT NOT NULL DEFAULT 0,
    duration_minutes INT NOT NULL,
    image_url VARCHAR(500) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_services_shop FOREIGN KEY (shop_id) REFERENCES shops(id),
    KEY idx_services_shop_status_sort (shop_id, status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS barbers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    shop_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    avatar_url VARCHAR(500) NULL,
    title VARCHAR(120) NULL,
    description TEXT NULL,
    specialties VARCHAR(500) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_barbers_shop FOREIGN KEY (shop_id) REFERENCES shops(id),
    KEY idx_barbers_shop_status_sort (shop_id, status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    appointment_no VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    shop_id BIGINT NOT NULL,
    barber_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    customer_name VARCHAR(120) NOT NULL,
    customer_phone VARCHAR(30) NOT NULL,
    remark VARCHAR(500) NULL,
    cancel_reason VARCHAR(500) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_appointments_no (appointment_no),
    CONSTRAINT fk_appointments_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_appointments_shop FOREIGN KEY (shop_id) REFERENCES shops(id),
    CONSTRAINT fk_appointments_barber FOREIGN KEY (barber_id) REFERENCES barbers(id),
    CONSTRAINT fk_appointments_service FOREIGN KEY (service_id) REFERENCES services(id),
    KEY idx_appointments_user_created (user_id, created_at),
    KEY idx_appointments_barber_date_time (barber_id, appointment_date, start_time, end_time),
    KEY idx_appointments_shop_date_status (shop_id, appointment_date, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
