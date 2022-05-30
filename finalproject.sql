

CREATE TABLE mail (
    sender character varying(50) NOT NULL,
    receiver character varying(50) NOT NULL,
    subject character varying(100) NOT NULL,
    body text NOT NULL,
    "time" timestamp without time zone NOT NULL,
    digest text
);

CREATE TABLE public_keys (
    pub bigint NOT NULL,
    utente character varying(50) NOT NULL,
    n bigint NOT NULL
);


CREATE TABLE users (
    name character varying(50) NOT NULL,
    surname character varying(50) NOT NULL,
    email character varying(50) NOT NULL,
    password character varying(60) NOT NULL
);


ALTER TABLE public_keys ADD CONSTRAINT public_keys_pk PRIMARY KEY (utente);

ALTER TABLE users ADD CONSTRAINT user_pk PRIMARY KEY (email);

ALTER TABLE mail ADD CONSTRAINT mail_fk FOREIGN KEY (sender) REFERENCES users(email);

ALTER TABLE mail ADD CONSTRAINT mail_fk_1 FOREIGN KEY (receiver) REFERENCES users(email);

ALTER TABLE public_keys ADD CONSTRAINT public_keys_users_email_fk
    FOREIGN KEY (utente) REFERENCES users(email) ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;