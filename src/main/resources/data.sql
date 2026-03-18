-- Insert authorities
-- docker exec -it leave_mysql mysql -uroot -p
-- curl "https://tallyfy.com/national-holidays/api/IN/2027.json"
INSERT INTO authority (id,name) VALUES
                                    (1,'ROLE_ADMIN'),
                                    (2,'ROLE_EMPLOYEE'),
                                    (3,'ROLE_MANAGER');


-- Insert employees FIRST (no manager yet)

INSERT INTO employee
(id,email,full_name,password_hash,is_active,created_at,manager_id)
VALUES
    (1,'dishankpatel1682003@gmail.com','Dishank',
     '$2a$10$Ni1msumLDy2Di8oy6CFV2u2xcM68810.jK1FvNIESVBK1kzZFsEQu',b'1',NOW(),NULL),

    (2,'dishankpatel16082003@gmail.com','Dishank Employee',
     '$2a$10$Ni1msumLDy2Di8oy6CFV2u2xcM68810.jK1FvNIESVBK1kzZFsEQu',b'1',NOW(),NULL),

    (3,'maheshbabujiit@gmail.com','Mahesh Babu',
     '$2a$10$Ni1msumLDy2Di8oy6CFV2u2xcM68810.jK1FvNIESVBK1kzZFsEQu',b'1',NOW(),NULL),

    (4,'user1@gmail.com','User 1',
     '$2a$10$Ni1msumLDy2Di8oy6CFV2u2xcM68810.jK1FvNIESVBK1kzZFsEQu',b'1',NOW(),NULL),

    (5,'user2@gmail.com','User 2',
     '$2a$10$Ni1msumLDy2Di8oy6CFV2u2xcM68810.jK1FvNIESVBK1kzZFsEQu',b'1',NOW(),NULL),

    (6,'user3@gmail.com','User 3',
     '$2a$10$Ni1msumLDy2Di8oy6CFV2u2xcM68810.jK1FvNIESVBK1kzZFsEQu',b'1',NOW(),NULL),

    (7,'user4@gmail.com','User 4',
     '$2a$10$Ni1msumLDy2Di8oy6CFV2u2xcM68810.jK1FvNIESVBK1kzZFsEQu',b'1',NOW(),NULL),

    (8,'user5@gmail.com','User 5',
     '$2a$10$Ni1msumLDy2Di8oy6CFV2u2xcM68810.jK1FvNIESVBK1kzZFsEQu',b'1',NOW(),NULL),

    (9,'user6@gmail.com','User 6',
     '$2a$10$Ni1msumLDy2Di8oy6CFV2u2xcM68810.jK1FvNIESVBK1kzZFsEQu',b'1',NOW(),NULL),

    (10,'user7@gmail.com','User 7',
     '$2a$10$Ni1msumLDy2Di8oy6CFV2u2xcM68810.jK1FvNIESVBK1kzZFsEQu',b'1',NOW(),NULL),

    (11,'user8@gmail.com','User 8',
     '$2a$10$Ni1msumLDy2Di8oy6CFV2u2xcM68810.jK1FvNIESVBK1kzZFsEQu',b'1',NOW(),NULL),

    (12,'user9@gmail.com','User 9',
     '$2a$10$Ni1msumLDy2Di8oy6CFV2u2xcM68810.jK1FvNIESVBK1kzZFsEQu',b'1',NOW(),NULL),

    (13,'user10@gmail.com','User 10',
     '$2a$10$Ni1msumLDy2Di8oy6CFV2u2xcM68810.jK1FvNIESVBK1kzZFsEQu',b'1',NOW(),NULL);


-- Now assign manager (Mahesh manages Dishank Employee)

UPDATE employee
SET manager_id = 3
WHERE id = 2;


-- Map roles

INSERT INTO employee_authority (employee_id,authority_id) VALUES
                                                              (1,1),
                                                              (2,2),
                                                              (3,3),
                                                              (4,2),
                                                              (5,2),
                                                              (6,2),
                                                              (7,2),
                                                              (8,2),
                                                              (9,2),
                                                              (10,2),
                                                              (11,2),
                                                              (12,2),
                                                              (13,2);