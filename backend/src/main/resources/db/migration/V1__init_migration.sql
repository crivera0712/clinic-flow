CREATE TYPE therapist_type_enum AS ENUM ('PHYSICAL_THERAPY_ASSISTANT', 'PHYSICAL_THERAPIST', 'OCCUPATIONAL_THERAPIST');

create table body_regions
(
    br_id        SERIAL PRIMARY KEY,
    code         varchar(30)          not null,
    display_name varchar(50)          not null,
    is_active    BOOLEAN DEFAULT TRUE not null
);

create table patients
(
    p_id       SERIAL PRIMARY KEY,
    first_name varchar(255) not null,
    last_name  varchar(255) not null
);

create table cases
(
    c_id  SERIAL PRIMARY KEY,
    p_id  int not null,
    br_id int not null,
    constraint case_body_regions_br_id_fk
        foreign key (br_id) references body_regions (br_id),
    constraint case_patients_p_id_fk
        foreign key (p_id) references patients (p_id)
);

create table therapists
(
    t_id           SERIAL PRIMARY KEY,
    name           varchar(255)        not null,
    therapist_type therapist_type_enum not null
);

create table appointments
(
    apt_id     SERIAL PRIMARY KEY,
    t_id       int       not null,
    c_id       int       not null,
    start_time TIMESTAMP not null,
    constraint appointments_case_c_id_fk
        foreign key (c_id) references cases (c_id),
    constraint appointments_therapists_t_id_fk
        foreign key (t_id) references therapists (t_id)
);
