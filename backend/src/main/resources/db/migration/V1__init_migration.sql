create table body_regions
(
    br_id        int auto_increment
        primary key,
    code         varchar(30)          not null,
    display_name varchar(50)          not null,
    is_active    tinyint(1) default 1 not null
);

create table patients
(
    p_id       int auto_increment
        primary key,
    first_name varchar(255) not null,
    last_name  varchar(255) not null
);

create table cases
(
    c_id  int auto_increment
        primary key,
    p_id  int not null,
    br_id int not null,
    constraint case_body_regions_br_id_fk
        foreign key (br_id) references body_regions (br_id),
    constraint case_patients_p_id_fk
        foreign key (p_id) references patients (p_id)
);

create table therapists
(
    t_id           int auto_increment
        primary key,
    name           varchar(255)                                                                        not null,
    therapist_type enum ('PHYSICAL_THERAPY_ASSISTANT', 'PHYSICAL_THERAPIST', 'OCCUPATIONAL_THERAPIST') not null
);

create table appointments
(
    apt_id     int auto_increment
        primary key,
    t_id       int      not null,
    c_id       int      not null,
    start_time datetime not null,
    constraint appointments_case_c_id_fk
        foreign key (c_id) references cases (c_id),
    constraint appointments_therapists_t_id_fk
        foreign key (t_id) references therapists (t_id)
);

