-- Run once in MySQL Workbench on `main_db` (shared by desktop + web).
CREATE TABLE IF NOT EXISTS support_tickets (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    body TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    admin_reply TEXT NULL,
    replied_at DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_support_user (user_id),
    CONSTRAINT fk_support_tickets_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
