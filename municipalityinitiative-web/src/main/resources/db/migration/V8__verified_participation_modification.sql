alter table verified_user_normal_initiatives drop constraint verified_user_normal_initiatives_initiative_pk;
alter table verified_user_normal_initiatives drop column id;

alter table verified_user_normal_initiatives add constraint verified_user_normal_initiatives_initiative_pk PRIMARY KEY (participant);

alter table verified_user_normal_initiatives add column verified boolean;

CREATE INDEX verified_user_normal_initiatives_participant_idx on verified_user_normal_initiatives(participant);
CREATE INDEX verified_user_normal_initiatives_verified_user_idx on verified_user_normal_initiatives(verified_user);