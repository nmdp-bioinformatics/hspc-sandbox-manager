# replace all occurrences of apps.hspconsortium.org with correct url

# app
UPDATE app
SET launch_uri=REPLACE(launch_uri, 'apps.hspconsortium.org/hspc-bilirubin-risk-chart/static/bilirubin-chart', 'bilirubin-risk-chart.hspconsortium.org')
WHERE launch_uri LIKE '%apps.hspconsortium.org/hspc-bilirubin-risk-chart/static/bilirubin-chart%';

UPDATE app
SET launch_uri=REPLACE(launch_uri, 'apps.hspconsortium.org/hspc-reference-apps/static/apps/cardiac-risk', 'cardiac-risk-app.hspconsortium.org')
WHERE launch_uri LIKE '%apps.hspconsortium.org/hspc-reference-apps/static/apps/cardiac-risk%';

UPDATE app
SET launch_uri=REPLACE(launch_uri, 'apps.hspconsortium.org/hspc-appointments/static/appointments', 'appointments.hspconsortium.org')
WHERE launch_uri LIKE '%apps.hspconsortium.org/hspc-appointments/static/appointments%';

UPDATE app
SET launch_uri=REPLACE(launch_uri, 'apps.hspconsortium.org/hspc-reference-apps/static/apps/bp-centiles', 'bp-centiles-app.hspconsortium.org')
WHERE launch_uri LIKE '%apps.hspconsortium.org/hspc-reference-apps/static/apps/bp-centiles%';

UPDATE app
SET launch_uri=REPLACE(launch_uri, 'apps.hspconsortium.org/hspc-patient-data-manager/static/patient-data-manager', 'growth-chart-app.hspconsortium.org')
WHERE launch_uri LIKE '%apps.hspconsortium.org/hspc-patient-data-manager/static/patient-data-manager%';

UPDATE app
SET launch_uri=REPLACE(launch_uri, 'apps.hspconsortium.org/hspc-reference-apps/static/apps/growth-chart', 'patient-data-manager.hspconsortium.org')
WHERE launch_uri LIKE '%apps.hspconsortium.org/hspc-reference-apps/static/apps/growth-chart%';

# auth_client
UPDATE auth_client
SET logo_uri=REPLACE(logo_uri, 'apps.hspconsortium.org/hspc-bilirubin-risk-chart/static/bilirubin-chart/images', 'content.hspconsortium.org/images/bilirubin/logo')
WHERE logo_uri LIKE '%apps.hspconsortium.org/hspc-bilirubin-risk-chart/static/bilirubin-chart/images%';

UPDATE auth_client
SET logo_uri=REPLACE(logo_uri, 'apps.hspconsortium.org/hspc-reference-apps/static/apps/fhir-app/images/HSPC-icon-S.png', 'content.hspconsortium.org/images/hspc/icon/HSPCSandboxNoIconApp-210x150.png')
WHERE logo_uri LIKE '%apps.hspconsortium.org/hspc-reference-apps/static/apps/fhir-app/images/HSPC-icon-S.png%';


