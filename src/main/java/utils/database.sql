create table public_keys
(
    n    bigint,
    pub    bigint,
    utente varchar(50) not null
        constraint public_keys_pk
            primary key
        constraint public_keys_users_email_fk
            references users
            on update cascade on delete cascade
            deferrable initially deferred
);

alter table public_keys
    owner to postgres;

create unique index public_keys_pub_uindex
    on public_keys (pub);

