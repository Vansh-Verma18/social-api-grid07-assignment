-- Initial test data for Social API
-- This file will be automatically executed by Spring Boot on startup

-- Insert test users (IDs 1-4)
INSERT INTO users (id, username, is_premium) VALUES 
(1, 'john_doe', false),
(2, 'jane_smith', true),
(3, 'alice_wonder', false),
(4, 'bob_builder', false)
ON CONFLICT (id) DO NOTHING;

-- Insert test bots (IDs 100-103)
INSERT INTO bots (id, name, persona_description) VALUES 
(100, 'TechBot', 'A friendly bot that loves discussing technology and programming'),
(101, 'NewsBot', 'A bot that shares interesting news and current events'),
(102, 'FunBot', 'A humorous bot that makes jokes and shares memes'),
(103, 'HelperBot', 'A helpful bot that provides assistance and answers questions')
ON CONFLICT (id) DO NOTHING;

-- Reset sequences to avoid ID conflicts
SELECT setval('users_id_seq', 10, false);
SELECT setval('bots_id_seq', 110, false);

-- Note: Posts and comments should be created via API to properly trigger Redis operations
