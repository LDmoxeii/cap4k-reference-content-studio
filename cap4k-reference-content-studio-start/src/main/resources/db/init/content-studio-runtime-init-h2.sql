-- business schema bootstrap
runscript from 'classpath:db/schema/content-studio-schema.sql';

-- frame slice required by cap4k-ddd-starter + ddd-integration-event-http-jpa
-- add H2-compatible definitions for:
-- __event
-- __archived_event
-- __event_http_subscriber
-- __locker
