-- TODO: Indexes

create table verified_user(
    id bigserial,
    hash varchar(64) constraint inituser_hash_nn not null, -- sha256(ssn & sharedSecret)
    name varchar(100),
    phone varchar(30),
    address varchar(256),
    email varchar(100),


    constraint verified_user_pk primary key (id),
    constraint verified_user_hash_u unique (hash)
);
create index verified_user_id_index on verified_user(id);
create index verified_user_hash_index on verified_user(hash);

create table verified_author (
    initiative_id bigserial,
    verified_user_id bigserial,
    constraint verified_author_pk primary key (initiative_id, verified_user_id)
);
create index verified_author_initiative_id_index on verified_author(initiative_id);

create table verified_participant (
    initiative_id bigserial,
    verified_user_id bigserial,

	participate_time date default now(),
    show_name boolean constraint verified_participant_show_name_nn not null,

    constraint verified_participant_pk primary key (initiative_id, verified_user_id)
);
create index verified_participant_initiative_id_index on verified_participant(initiative_id);
