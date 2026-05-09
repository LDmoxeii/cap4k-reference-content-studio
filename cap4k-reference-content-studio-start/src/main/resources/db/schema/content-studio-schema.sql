create table if not exists content (
    id uuid primary key,
    title varchar(200) not null,
    body clob not null,
    media_source_key varchar(200) not null,
    review_status int not null comment '@T=ReviewStatus;@E=0:PENDING:Pending|1:APPROVED:Approved;',
    content_status int not null comment '@T=ContentStatus;@E=0:DRAFT:Draft|1:PUBLISHED:Published;',
    reviewer_id uuid,
    reviewed_at timestamp,
    published_at timestamp,
    db_created_at timestamp not null,
    db_updated_at timestamp not null
);

create table if not exists media_processing_task (
    id uuid primary key,
    content_id uuid not null,
    external_task_id varchar(120),
    processing_status int not null comment '@T=MediaProcessingStatus;@E=0:PENDING:Pending|1:SUBMITTED:Submitted|2:SUCCEEDED:Succeeded;',
    db_created_at timestamp not null,
    db_updated_at timestamp not null,
    constraint uq_media_processing_task_content_id unique (content_id),
    constraint fk_media_processing_task_content foreign key (content_id) references content(id)
);
