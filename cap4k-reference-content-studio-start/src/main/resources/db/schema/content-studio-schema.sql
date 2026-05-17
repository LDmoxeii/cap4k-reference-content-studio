create table if not exists content (
    id uuid primary key,
    title varchar(200) not null,
    body clob not null,
    media_source_key varchar(200) not null,
    review_status int not null comment '@T=ReviewStatus;@E=0:PENDING:Pending|1:APPROVED:Approved;',
    content_status int not null comment '@T=ContentStatus;@E=0:DRAFT:Draft|1:PUBLISHED:Published;',
    release_policy int not null comment '@T=ReleasePolicy;@E=0:IMMEDIATE:Immediate|2:PAID:Paid;',
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

create table if not exists paid_publication_task (
    id uuid primary key,
    content_id uuid not null,
    paid_publication_status int not null comment '@T=PaidPublicationStatus;@E=0:PENDING:Pending|1:RUNNING:Running|2:PUBLISHED:Published|3:FAILED:Failed|4:REQUIRES_OPERATOR_REPAIR:Requires operator repair;',
    publication_saga_id varchar(64),
    payout_hold_status int not null comment '@T=PayoutHoldStatus;@E=0:NONE:None|1:RESERVED:Reserved|2:RELEASED:Released|3:CAPTURED:Captured;',
    payout_hold_id varchar(120),
    entitlement_plan_status int not null comment '@T=EntitlementPlanStatus;@E=0:NONE:None|1:CREATED:Created|2:ACTIVATED:Activated|3:CANCELLED:Cancelled;',
    entitlement_plan_id varchar(120),
    started_at timestamp,
    published_at timestamp,
    completed_at timestamp,
    failed_at timestamp,
    failed_reason varchar(1000),
    db_created_at timestamp not null,
    db_updated_at timestamp not null,
    constraint uq_paid_publication_task_content_id unique (content_id),
    constraint fk_paid_publication_task_content foreign key (content_id) references content(id)
);
