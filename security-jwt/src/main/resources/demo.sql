INSERT INTO USERS (USER_ID, USERNAME, PASSWORD, NICKNAME, USE_YN) VALUES (1, 'admin', '$2a$08$lDnHPz7eUkSi6ao14Twuau08mzhWrL4kyZGGU5xfiGALO/Vxd5DOi', 'admin', true);
INSERT INTO USERS (USER_ID, USERNAME, PASSWORD, NICKNAME, USE_YN) VALUES (2, 'user', '$2a$10$74cg21HO1i5WR0JHab3/c.MJgnbswYpQNjHH0qaZrOSljYiPx40nS', 'user', true);

INSERT INTO AUTHORITY (AUTHORITY_ID, AUTHORITY_NAME) values (1, 'ROLE_USER');
INSERT INTO AUTHORITY (AUTHORITY_ID, AUTHORITY_NAME) values (2, 'ROLE_ADMIN');