-- business schema bootstrap
runscript from 'classpath:db/schema/content-studio-schema.sql';

-- frame slice required by cap4k-ddd-starter + ddd-integration-event-http-jpa
-- add H2-compatible definitions for:
-- __event
-- __archived_event
-- __request
-- __archived_request
-- __event_http_subscriber
-- __locker

create table if not exists __event (
    id bigint auto_increment primary key,
    event_uuid varchar(64) not null default '',
    svc_name varchar(255) not null default '',
    event_type varchar(255) not null default '',
    data clob,
    data_type varchar(255) not null default '',
    exception clob,
    expire_at timestamp not null default current_timestamp,
    create_at timestamp not null default current_timestamp,
    event_state int not null default 0,
    last_try_time timestamp not null default current_timestamp,
    next_try_time timestamp not null default current_timestamp,
    tried_times int not null default 0,
    try_times int not null default 0,
    version int not null default 0,
    db_created_at timestamp not null default current_timestamp,
    db_updated_at timestamp not null default current_timestamp
);

create index if not exists idx___event_event_uuid on __event(event_uuid);
create index if not exists idx___event_event_type_svc_name on __event(event_type, svc_name);
create index if not exists idx___event_next_try_time on __event(next_try_time);

create table if not exists __archived_event (
    id bigint primary key,
    event_uuid varchar(64) not null default '',
    svc_name varchar(255) not null default '',
    event_type varchar(255) not null default '',
    data clob,
    data_type varchar(255) not null default '',
    exception clob,
    expire_at timestamp not null default current_timestamp,
    create_at timestamp not null default current_timestamp,
    event_state int not null default 0,
    last_try_time timestamp not null default current_timestamp,
    next_try_time timestamp not null default current_timestamp,
    tried_times int not null default 0,
    try_times int not null default 0,
    version int not null default 0,
    db_created_at timestamp not null default current_timestamp,
    db_updated_at timestamp not null default current_timestamp
);

create index if not exists idx___archived_event_event_uuid on __archived_event(event_uuid);
create index if not exists idx___archived_event_event_type_svc_name on __archived_event(event_type, svc_name);

create table if not exists __request (
    id bigint auto_increment primary key,
    request_uuid varchar(64) not null default '',
    svc_name varchar(255) not null default '',
    request_type varchar(255) not null default '',
    param clob,
    param_type varchar(255) not null default '',
    result clob,
    result_type varchar(255) not null default '',
    exception clob,
    expire_at timestamp not null default current_timestamp,
    create_at timestamp not null default current_timestamp,
    request_state int not null default 0,
    last_try_time timestamp not null default current_timestamp,
    next_try_time timestamp not null default timestamp '1970-01-01 00:00:00',
    tried_times int not null default 0,
    try_times int not null default 0,
    version int not null default 0,
    db_created_at timestamp not null default current_timestamp,
    db_updated_at timestamp not null default current_timestamp
);

create index if not exists idx___request_db_created_at on __request(db_created_at);
create index if not exists idx___request_db_updated_at on __request(db_updated_at);
create index if not exists idx___request_request_uuid on __request(request_uuid);
create index if not exists idx___request_request_type on __request(request_type, svc_name);
create index if not exists idx___request_create_at on __request(create_at);
create index if not exists idx___request_expire_at on __request(expire_at);
create index if not exists idx___request_next_try_time on __request(next_try_time);

create table if not exists __archived_request (
    id bigint primary key,
    request_uuid varchar(64) not null default '',
    svc_name varchar(255) not null default '',
    request_type varchar(255) not null default '',
    param clob,
    param_type varchar(255) not null default '',
    result clob,
    result_type varchar(255) not null default '',
    exception clob,
    expire_at timestamp not null default current_timestamp,
    create_at timestamp not null default current_timestamp,
    request_state int not null default 0,
    last_try_time timestamp not null default current_timestamp,
    next_try_time timestamp not null default timestamp '1970-01-01 00:00:00',
    tried_times int not null default 0,
    try_times int not null default 0,
    version int not null default 0,
    db_created_at timestamp not null default current_timestamp,
    db_updated_at timestamp not null default current_timestamp
);

create index if not exists idx___archived_request_db_created_at on __archived_request(db_created_at);
create index if not exists idx___archived_request_db_updated_at on __archived_request(db_updated_at);
create index if not exists idx___archived_request_request_uuid on __archived_request(request_uuid);
create index if not exists idx___archived_request_request_type on __archived_request(request_type, svc_name);
create index if not exists idx___archived_request_create_at on __archived_request(create_at);
create index if not exists idx___archived_request_expire_at on __archived_request(expire_at);
create index if not exists idx___archived_request_next_try_time on __archived_request(next_try_time);

create table if not exists __event_http_subscriber (
    id bigint auto_increment primary key,
    event varchar(255) not null default '',
    subscriber varchar(255) not null default '',
    callback_url varchar(1024) not null default '',
    version int not null default 0,
    db_created_at timestamp not null default current_timestamp,
    db_updated_at timestamp not null default current_timestamp,
    db_deleted tinyint not null default 0
);

create index if not exists idx___event_http_subscriber_event on __event_http_subscriber(event);

create table if not exists __locker (
    id bigint auto_increment primary key,
    name varchar(100) not null default '',
    pwd varchar(100) not null default '',
    lock_at timestamp not null default timestamp '1970-01-01 00:00:00',
    unlock_at timestamp not null default timestamp '1970-01-01 00:00:00',
    version bigint not null default 0,
    db_created_at timestamp not null default current_timestamp,
    db_updated_at timestamp not null default current_timestamp,
    constraint uq___locker_name unique (name)
);
