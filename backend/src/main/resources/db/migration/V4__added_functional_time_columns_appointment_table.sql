alter table appointments
    change start_time scheduled_at datetime not null;

alter table appointments
    add created_at datetime not null;

alter table appointments
    add modified_at datetime null;