create database "Hotel";

create table occupied
(
    id         integer not null UNIQUE primary key,
    date_begin date,
    date_end   date,
    name       varchar(200),
    room       integer
);

create table rooms
(
    number integer not null UNIQUE primary key,
    people integer not null,
    price  integer not null
);


GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public to app;
