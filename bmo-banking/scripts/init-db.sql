-- BMO Banking - Database Initialization Script
-- Runs automatically when MySQL container starts

CREATE DATABASE IF NOT EXISTS bmo_accounts;
CREATE DATABASE IF NOT EXISTS bmo_transactions;
CREATE DATABASE IF NOT EXISTS bmo_payments;

-- Grant all privileges to root (for local dev)
GRANT ALL PRIVILEGES ON bmo_accounts.*    TO 'root'@'%';
GRANT ALL PRIVILEGES ON bmo_transactions.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON bmo_payments.*    TO 'root'@'%';
FLUSH PRIVILEGES;
