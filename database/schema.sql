CREATE DATABASE IF NOT EXISTS lifelink CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lifelink;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(190) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  role ENUM('ADMIN','DONOR','RECIPIENT','BLOOD_BANK') NOT NULL,
  status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS donor_profiles (
  user_id BIGINT PRIMARY KEY,
  blood_group ENUM('A+','A-','B+','B-','AB+','AB-','O+','O-') NOT NULL,
  phone VARCHAR(20),
  city VARCHAR(100),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS recipient_profiles (
  user_id BIGINT PRIMARY KEY,
  phone VARCHAR(20),
  city VARCHAR(100),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS blood_banks (
  user_id BIGINT PRIMARY KEY,
  bank_name VARCHAR(160) NOT NULL,
  address VARCHAR(255) NOT NULL,
  city VARCHAR(100) NOT NULL,
  phone VARCHAR(20),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS blood_inventory (
  bank_user_id BIGINT NOT NULL,
  blood_group ENUM('A+','A-','B+','B-','AB+','AB-','O+','O-') NOT NULL,
  units INT NOT NULL DEFAULT 0,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (bank_user_id,blood_group),
  CHECK (units >= 0),
  FOREIGN KEY (bank_user_id) REFERENCES blood_banks(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS donation_appointments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  donor_user_id BIGINT NOT NULL,
  bank_user_id BIGINT NOT NULL,
  scheduled_at DATETIME NOT NULL,
  status ENUM('PENDING','APPROVED','COMPLETED','CANCELLED','REJECTED') NOT NULL DEFAULT 'PENDING',
  notes VARCHAR(500),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (donor_user_id) REFERENCES donor_profiles(user_id),
  FOREIGN KEY (bank_user_id) REFERENCES blood_banks(user_id)
);

CREATE TABLE IF NOT EXISTS blood_requests (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  recipient_user_id BIGINT NOT NULL,
  bank_user_id BIGINT NOT NULL,
  blood_group ENUM('A+','A-','B+','B-','AB+','AB-','O+','O-') NOT NULL,
  units INT NOT NULL,
  status ENUM('PENDING','APPROVED','FULFILLED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CHECK (units > 0),
  FOREIGN KEY (recipient_user_id) REFERENCES recipient_profiles(user_id),
  FOREIGN KEY (bank_user_id) REFERENCES blood_banks(user_id)
);

CREATE TABLE IF NOT EXISTS transfer_requests (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  from_bank_user_id BIGINT NOT NULL,
  to_bank_user_id BIGINT NOT NULL,
  blood_group ENUM('A+','A-','B+','B-','AB+','AB-','O+','O-') NOT NULL,
  units INT NOT NULL,
  status ENUM('PENDING','APPROVED','REJECTED','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CHECK (units > 0),
  CHECK (from_bank_user_id <> to_bank_user_id),
  FOREIGN KEY (from_bank_user_id) REFERENCES blood_banks(user_id),
  FOREIGN KEY (to_bank_user_id) REFERENCES blood_banks(user_id)
);
