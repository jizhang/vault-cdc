create table extract_table (
  id int primary key auto_increment
  ,source_instance int not null
  ,source_database varchar(255) not null
  ,source_table varchar(255) not null
  ,target_instance int not null
  ,target_database varchar(255) not null
  ,target_table varchar(255) not null
  ,column_list varchar(2000) not null default ''
  ,created_at datetime not null
  ,updated_at timestamp not null default current_timestamp on update current_timestamp
);

create table database_instance (
  id int primary key auto_increment
  ,name varchar(255) not null
  ,host varchar(255) not null
  ,port int not null
  ,username varchar(255) not null
  ,password varchar(255) not null
  ,created_at datetime not null
  ,updated_at timestamp not null default current_timestamp on update current_timestamp
);

create table user (
  id int not null primary key auto_increment
  ,username varchar(255) not null
  ,password varchar(255) not null
  ,created_at datetime not null
  ,updated_at timestamp not null default current_timestamp on update current_timestamp
  ,unique key uk_username (username)
);
