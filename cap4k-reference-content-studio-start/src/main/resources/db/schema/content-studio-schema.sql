create table if not exists content (
    id uuid primary key,
    title varchar(200) not null,
    body clob not null,
    media_source_key varchar(200) not null,
    review_status int not null comment '@T=ReviewStatus;@E=0:PENDING:Pending|1:APPROVED:Approved;',
    content_status int not null comment '@T=ContentStatus;@E=0:DRAFT:Draft|1:PUBLISHED:Published;',
    release_policy int not null comment '@T=ReleasePolicy;@E=0:IMMEDIATE:Immediate|1:GATED:Gated;',
    release_window_opens_at timestamp,
    release_window_closes_at timestamp,
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
    result_snapshot clob comment '@T=MediaProcessingResultSnapshot;',
    db_created_at timestamp not null,
    db_updated_at timestamp not null,
    constraint uq_media_processing_task_content_id unique (content_id),
    constraint fk_media_processing_task_content foreign key (content_id) references content(id)
);

create table if not exists publication_release_readiness (
    id uuid primary key,
    content_id uuid not null,
    media_processing_task_id uuid not null,
    readiness_state int not null comment '@T=PublicationReleaseReadinessState;@E=0:WAITING:Waiting|1:READY:Ready|2:CANCELLED:Cancelled|3:EXPIRED:Expired;',
    copyright_status int not null comment '@T=CopyrightReviewStatus;@E=0:WAITING:Waiting|1:PASSED:Passed|2:REJECTED:Rejected;',
    manual_confirmation_status int not null comment '@T=ManualReleaseConfirmationStatus;@E=0:WAITING:Waiting|1:CONFIRMED:Confirmed;',
    release_window_opens_at timestamp not null,
    release_window_closes_at timestamp not null,
    release_saga_id varchar(64),
    ready_at timestamp,
    cancel_reason varchar(500),
    db_created_at timestamp not null,
    db_updated_at timestamp not null,
    constraint uq_publication_release_readiness_content_id unique (content_id),
    constraint fk_publication_release_readiness_content foreign key (content_id) references content(id),
    constraint fk_publication_release_readiness_task foreign key (media_processing_task_id) references media_processing_task(id)
);
