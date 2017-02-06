drop table sandman.user_launch_scenario;
ALTER TABLE sandman.launch_scenario DROP foreign key FK_jsc0b9p1hfvqdknki9944e5fn;
ALTER TABLE sandman.launch_scenario DROP INDEX FK_jsc0b9p1hfvqdknki9944e5fn;
alter table sandman.launch_scenario drop column persona_id;
drop table sandman.persona;


SET SQL_SAFE_UPDATES = 0;
-- set visibility on Apps, Launch Scenarios and User Personas
update sandman.app as a, sandman.auth_client as ac set a.visibility = 0 where ac.id = a.auth_client_id AND ac.auth_database_id is not NULL;
update sandman.launch_scenario as ls set ls.visibility = 0;
update sandman.user_persona as up set up.visibility = 0;
-- set created_by's to sandbox creator if null
UPDATE sandman.app as a Join sandman.sandbox s on s.id = a.sandbox_id and a.created_by_id is NULL set a.created_by_id = s.created_by_id;
UPDATE sandman.user_persona as up Join sandman.sandbox s on s.id = up.sandbox_id and up.created_by_id is NULL set up.created_by_id = s.created_by_id;

-- set visibility on Sandboxes
update sandman.sandbox sb set sb.visibility = 1;
SET SQL_SAFE_UPDATES = 1;


drop procedure if exists SetUpSystemRoles;
delimiter #
CREATE PROCEDURE SetUpSystemRoles()
BEGIN
  DECLARE done INT DEFAULT FALSE;
  DECLARE userId INT(11);    -- or approriate type

  DECLARE curs CURSOR FOR  SELECT id FROM sandman.user;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

  OPEN curs;

  read_loop: LOOP
    FETCH curs INTO userId;
    IF done THEN
      LEAVE read_loop;
    END IF;

       INSERT INTO sandman.system_role (user_id, role) VALUES (userId, 1);
       INSERT INTO sandman.system_role (user_id, role) VALUES (userId, 2);
  END LOOP;

  CLOSE curs;
END
#
delimiter ;

drop procedure if exists SetUpSandboxRoles;
delimiter #

CREATE PROCEDURE SetUpSandboxRoles()
BEGIN
  DECLARE done INT DEFAULT FALSE;

  DECLARE userId INT(11);    -- or approriate type
  DECLARE sandboxId INT(11);    -- or approriate type

  DECLARE curs CURSOR FOR  SELECT user_id, sandbox_id FROM sandman.user_sandbox;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

  OPEN curs;

  read_loop: LOOP
    FETCH curs INTO userId, sandboxId;
    IF done THEN
      LEAVE read_loop;
    END IF;

       IF NOT EXISTS (SELECT ur.id FROM sandman.user_role as ur, sandman.sandbox_user_roles as sur where ur.id = sur.user_roles and ur.user_id = userId and sur.sandbox = sandboxId and (ur.role = 1 or ur.role = 0)) THEN
          INSERT INTO sandman.user_role (role, user_id)  VALUES (1, userId);
          INSERT INTO sandman.sandbox_user_roles (sandbox, user_roles)  VALUES (sandboxId,LAST_INSERT_ID());
       END IF;

       INSERT INTO sandman.user_role (role, user_id) VALUES (3, userId);
       INSERT INTO sandman.sandbox_user_roles (sandbox, user_roles)  VALUES (sandboxId,LAST_INSERT_ID());
       INSERT INTO sandman.user_role (role, user_id)  VALUES (4, userId);
       INSERT INTO sandman.sandbox_user_roles (sandbox, user_roles)  VALUES (sandboxId,LAST_INSERT_ID());

  END LOOP;

  CLOSE curs;
END

#

delimiter ;

call SetUpSystemRoles();
call SetUpSandboxRoles();

drop procedure if exists SetUpSystemRoles;
drop procedure if exists SetUpSandboxRoles;





-- Orphaned Annon Apps