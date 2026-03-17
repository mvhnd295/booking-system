-- Default rate limits: 3 bookings/day, 2 reschedules/day
INSERT INTO app_config (key, value) VALUES ('MAX_BOOKING_PER_DAY', '3');
INSERT INTO app_config (key, value) VALUES ('MAX_RESCHEDULE_PER_DAY', '2');
