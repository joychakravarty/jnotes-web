CREATE KEYSPACE IF NOT EXISTS jnotes WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
use jnotes;

//DROP ROLE IF EXISTS jnotes_client;
//DROP ROLE IF EXISTS client_role;

//User - jnotes_client
//CREATE ROLE jnotes_client 
//WITH PASSWORD = '******************' 
//AND LOGIN = true;

//CREATE ROLE client_role;

//Run these on CSQ Console

//GRANT CREATE ON KEYSPACE jnotes TO client_role;
//GRANT client_role TO jnotes_client;

//LIST ALL PERMISSIONS OF jnotes_client;