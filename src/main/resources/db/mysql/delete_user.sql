/*
  This is a work in progress.
   TODO:
    * imports
    * personas
    * lunch contexts
 */
SET @user_id := 62;

/* activity log */
DELETE FROM sandbox_activity_log
WHERE user_id = @user_id;

/* system role */
DELETE FROM system_role
WHERE user_id = @user_id;

/* Sandboxes */
DELETE FROM user_sandbox
WHERE user_id = @user_id;

DELETE FROM sandbox_user_roles
WHERE user_roles IN (SELECT id
                     FROM user_role
                     WHERE user_id = @user_id);

DELETE FROM user_role
WHERE user_id = @user_id;

DELETE FROM sandbox
WHERE created_by_id = @user_id;

DELETE FROM user_terms_of_use_acceptance
WHERE user_id = @user_id;

DELETE FROM sandbox_invite
WHERE invitee_id = @user_id
      OR invited_by_id = @user_id;

/* finally, the actual user */
DELETE FROM user
WHERE id = @user_id;