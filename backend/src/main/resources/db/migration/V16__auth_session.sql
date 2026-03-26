create table auth_sessions
(
    id                 CHAR(36)                           primary key,
    user_id            BIGINT                             not null,
    revoked_at         DATETIME                           null,
    created_at         DATETIME default CURRENT_TIMESTAMP not null,
    refresh_expires_at DATETIME                           not null,
    constraint auth_sessions_users_id_fk
        foreign key (user_id) references users (id)
);
