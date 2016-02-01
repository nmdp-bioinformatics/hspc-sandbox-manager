CREATE DATABASE IF NOT EXISTS sandman;

USE sandman;

CREATE TABLE IF NOT EXISTS app (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	client_name VARCHAR(256),
	launch_uri VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS patient (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(256),
	fhir_id VARCHAR(256),
	resource VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS persona (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(256),
	fhir_id VARCHAR(256),
	resource VARCHAR(256),
	full_url VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS launch_scenario (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	description VARCHAR(256),
	last_launch TIMESTAMP NULL,
    patient_id BIGINT,
    persona_id BIGINT not null,
    app_id BIGINT,
    constraint fk_launchScenario_patient foreign key(patient_id) references patient(id),
    constraint fk_launchScenario_persona foreign key(persona_id) references persona(id),
    constraint fk_launchScenario_app foreign key(app_id) references app(id)
);

COMMIT;