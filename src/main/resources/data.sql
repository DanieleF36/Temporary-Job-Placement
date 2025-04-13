-- DOCUMENT DATA
INSERT INTO document_binary_data (content)
VALUES (decode('DEADBEEF', 'hex'));
INSERT INTO document_binary_data (content)
VALUES (decode('CAFEBABE', 'hex'));

-- DOCUMENT METADATA
INSERT INTO document_metadata (id, name, size, content_type, creation_timestamp)
VALUES (1, 'Document 1', 4, 'application/octet-stream', TIMESTAMPTZ '2025-04-13 12:00:00+00');
INSERT INTO document_metadata (id, name, size, content_type, creation_timestamp)
VALUES (2, 'Document 2', 4, 'application/pdf', TIMESTAMPTZ '2025-04-13 13:00:00+00');

-- EMAIL
INSERT INTO email (email) VALUES ('example1@example.com');
INSERT INTO email (email) VALUES ('example2@example.com');
-- ADDRESS
INSERT INTO address (address) VALUES ('Via Roma 1, Rome');
INSERT INTO address (address) VALUES ('Via Milano 2, Milan');
-- TELEPHONE
INSERT INTO telephone (prefix, number) VALUES (39, 123456789);
INSERT INTO telephone (prefix, number) VALUES (39, 987654321);

-- CONTACT
INSERT INTO contact (name, surname, email_id, address_id, telephone_id, ssn, category)
VALUES ('Mario', 'Rossi', 1, 1, 1, 'RSSMRA80A01H501U', 0);
INSERT INTO contact (name, surname, email_id, address_id, telephone_id, ssn, category)
VALUES ('Luigi', 'Verdi', 2, 2, 2, 'VRDLGU80B02H502U', 1);

-- MESSAGE
INSERT INTO message (sender_id, date, subject, body, channel, priority, state)
VALUES (1, TIMESTAMPTZ '2025-04-13 14:00:00+00', 'Hello', 'First message body', 1, 1, 0);
INSERT INTO message (sender_id, date, subject, body, channel, priority, state)
VALUES (2, TIMESTAMPTZ '2025-04-13 15:00:00+00', 'Hi', 'Second message body', 2, 2, 1);

-- ACTION
INSERT INTO action (message_id, state, date, comment)
VALUES (1, 1, TIMESTAMPTZ '2025-04-13 16:00:00+00', 'Action comment 1');
INSERT INTO action (message_id, state, date, comment)
VALUES (2, 3, TIMESTAMPTZ '2025-04-13 17:00:00+00', 'Action comment 2');