drop table sandman.user_launch_scenario;
drop table sandman.persona;
ALTER TABLE sandman.launch_scenario DROP foreign key FK_jsc0b9p1hfvqdknki9944e5fn;
ALTER TABLE sandman.launch_scenario DROP INDEX FK_jsc0b9p1hfvqdknki9944e5fn;
alter table sandman.launch_scenario drop column persona_id;