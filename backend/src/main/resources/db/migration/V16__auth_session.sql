create table auth_sessions
(
    id                 UUID                                PRIMARY KEY,
    user_id            BIGINT                              not null,
    revoked_at         TIMESTAMP                           null,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP not null,
    refresh_expires_at TIMESTAMP                           not null,
    constraint auth_sessions_users_id_fk
        foreign key (user_id) references users (id)
);
