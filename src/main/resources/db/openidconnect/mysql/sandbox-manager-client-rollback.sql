SET AUTOCOMMIT = 0;

START TRANSACTION;

-- Sandbox Manager
DELETE FROM whitelisted_site_scope WHERE owner_id = (SELECT id from whitelisted_site where client_id = 'sand_man');
DELETE FROM whitelisted_site WHERE client_id = 'sand_man';
DELETE FROM client_grant_type WHERE owner_id = (SELECT id from client_details where client_id = 'sand_man');
DELETE FROM client_scope WHERE owner_id = (SELECT id from client_details where client_id = 'sand_man');
DELETE FROM client_redirect_uri WHERE owner_id = (SELECT id from client_details where client_id = 'sand_man');
DELETE FROM client_details WHERE client_id = 'sand_man';

COMMIT;

SET AUTOCOMMIT = 1;
