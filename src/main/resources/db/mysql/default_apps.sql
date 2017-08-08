# Note: these pre-configured values aren't supported because the auth_database_id is not known.
# todo: come up with a method of allowing preregistration of existing client apps

# INSERT INTO auth_client (id, auth_database_id, client_id, client_name, logo_uri)
# VALUES
#   (1, 8, 'bp_centiles', 'BP Centiles', 'http://localhost:8093/static/images/apps/bpc.png'),
#   (2, 9, 'cardiac_risk', 'Cardiac Risk', 'http://localhost:8093/static/images/apps/cardio.png'),
#   (3, 18, 'growth_chart', 'Growth Chart', 'http://localhost:8093/static/images/apps/pgc.png');
#
# INSERT INTO app (id, created_timestamp, launch_uri, logo_uri, visibility, auth_client_id, created_by_id, logo_id, sandbox_id)
# VALUES
#   (1, now(), 'http://localhost:8093/static/apps/bp-centiles/launch.html',
#    'http://localhost:8093/static/images/apps/bpc.png', 0, 1, 1, NULL, 1),
#   (2, now(), 'http://localhost:8093/static/apps/cardiac-risk/launch.html',
#    'http://localhost:8093/static/images/apps/cardio.png', 0, 2, 1, NULL, 1),
#   (3, now(), 'http://localhost:8093/static/apps/growth-chart/launch.html',
#    'http://localhost:8093/static/images/apps/pgc.png', 0, 3, 1, NULL, 1),
#   (4, now(), 'http://localhost:8093/static/apps/bp-centiles/launch.html',
#    'http://localhost:8093/static/images/apps/bpc.png', 0, 1, 1, NULL, 2),
#   (5, now(), 'http://localhost:8093/static/apps/cardiac-risk/launch.html',
#    'http://localhost:8093/static/images/apps/cardio.png', 0, 2, 1, NULL, 2),
#   (6, now(), 'http://localhost:8093/static/apps/growth-chart/launch.html',
#    'http://localhost:8093/static/images/apps/pgc.png', 0, 3, 1, NULL, 2);
