-- Clear existing data to prevent unique constraint violations on restart
DELETE FROM internal_users;

-- Insert designated staff members
INSERT INTO internal_users (name, email, department ,is_designated_recipient)
VALUES ('Sukhpreet Khurana', 'admin@testdomain.com', 'IT',true);

INSERT INTO internal_users (name, email, department ,is_designated_recipient)
VALUES ('John Doe', 'alerts@testdomain.com', 'Sales',true);

-- Optional: Insert a non-designated user to test the filter logic
INSERT INTO internal_users (name, email, department ,is_designated_recipient)
VALUES ('Jane Doe', 'user@testdomain.com', 'Marketing',false);