-- Default seed data for local MVP preview.
-- Safe to run repeatedly.
SET NAMES utf8mb4;
SET time_zone = '+08:00';

INSERT INTO users (id, openid, nickname, avatar_url, phone, status)
VALUES (1, 'dev-openid', '体验用户', NULL, '13800000000', 'ACTIVE')
ON DUPLICATE KEY UPDATE
    nickname = VALUES(nickname),
    phone = VALUES(phone),
    status = VALUES(status);

INSERT INTO shops (id, name, address, phone, description, opening_time, closing_time, status)
VALUES (1, '拾光男士理发馆', '上海市静安区南京西路 888 号 2F', '021-88886666', '预约制精品理发，不排队更从容。到店请提前 5 分钟签到，如需取消请至少提前 2 小时操作。', '10:00:00', '21:00:00', 'ACTIVE')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    address = VALUES(address),
    phone = VALUES(phone),
    description = VALUES(description),
    opening_time = VALUES(opening_time),
    closing_time = VALUES(closing_time),
    status = VALUES(status);

INSERT INTO services (id, shop_id, name, description, price_cents, duration_minutes, image_url, status, sort_order)
VALUES
    (1, 1, '男士精剪', '适合日常清爽造型，含洗吹。', 8800, 45, NULL, 'ACTIVE', 10),
    (2, 1, '总监设计剪', '根据脸型和职业风格定制。', 16800, 60, NULL, 'ACTIVE', 20),
    (3, 1, '烫发造型', '纹理烫、韩式微分等造型。', 39800, 150, NULL, 'ACTIVE', 30),
    (4, 1, '染发护理', '低刺激染发，含基础护理。', 32800, 120, NULL, 'ACTIVE', 40)
ON DUPLICATE KEY UPDATE
    shop_id = VALUES(shop_id),
    name = VALUES(name),
    description = VALUES(description),
    price_cents = VALUES(price_cents),
    duration_minutes = VALUES(duration_minutes),
    status = VALUES(status),
    sort_order = VALUES(sort_order);

INSERT INTO barbers (id, shop_id, name, avatar_url, title, description, specialties, status, sort_order)
VALUES
    (1, 1, '阿泽', NULL, '首席理发师', '8 年男士理发经验，擅长清爽短发和商务造型。', '男士短发 / 油头 / 商务造型', 'ACTIVE', 10),
    (2, 1, 'Kevin', NULL, '高级理发师', '6 年造型经验，适合年轻化和纹理造型。', '韩式纹理 / 微分碎盖 / 烫发', 'ACTIVE', 20),
    (3, 1, 'Leo', NULL, '造型总监', '10 年经验，擅长整体形象设计。', '形象设计 / 长短发改造', 'ACTIVE', 30)
ON DUPLICATE KEY UPDATE
    shop_id = VALUES(shop_id),
    name = VALUES(name),
    title = VALUES(title),
    description = VALUES(description),
    specialties = VALUES(specialties),
    status = VALUES(status),
    sort_order = VALUES(sort_order);
