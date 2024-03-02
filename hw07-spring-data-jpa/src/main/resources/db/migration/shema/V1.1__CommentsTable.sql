create table comments (
    id bigserial,
    text varchar(512),
    book_id bigint references books(id) on delete cascade,
    primary key (id)
);