-- DOCUMENT
CREATE TABLE document_binary_data (
    id SERIAL PRIMARY KEY,
    content BYTEA NOT NULL
);

CREATE TABLE document_metadata (
    id INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    size INT NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    creation_timestamp TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_document_binary FOREIGN KEY (id) REFERENCES document_binary_data(id)
);

-- CONTACT
CREATE TABLE contact (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    surname VARCHAR(255) NOT NULL,
    email_id INT,
    address_id INT,
    telephone_id INT,
    ssn VARCHAR(255),
    category INT NOT NULL,
    CONSTRAINT fk_contact_email FOREIGN KEY (email_id) REFERENCES email(id),
    CONSTRAINT fk_contact_address FOREIGN KEY (address_id) REFERENCES address(id),
    CONSTRAINT fk_contact_telephone FOREIGN KEY (telephone_id) REFERENCES telephone(id),
    CONSTRAINT chk_contact_category CHECK (category IN (0, 1, 2))
);

CREATE TABLE address (
    id SERIAL PRIMARY KEY,
    address VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE email (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE telephone (
    id SERIAL PRIMARY KEY,
    prefix INT NOT NULL,
    number INT NOT NULL
);

-- MESSAGE
CREATE TABLE message (
     id SERIAL PRIMARY KEY,
     sender_id INT NOT NULL,
     date TIMESTAMPTZ NOT NULL,
     subject VARCHAR(255),
     body TEXT,
     channel INT NOT NULL,
     priority INT NOT NULL,
     state INT NOT NULL,
     CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES contact(id),
     CONSTRAINT chk_message_channel CHECK (channel IN (0, 1, 2)),
     CONSTRAINT chk_message_state CHECK (state IN (0, 1, 2, 3, 4, 5))
);

CREATE TABLE action (
    id SERIAL PRIMARY KEY,
    message_id INT,
    state INT NOT NULL,
    date TIMESTAMPTZ NOT NULL,
    comment TEXT,
    CONSTRAINT fk_action_message FOREIGN KEY (message_id) REFERENCES message(id),
    CONSTRAINT chk_action_state CHECK (state IN (0, 1, 2, 3, 4, 5))
);