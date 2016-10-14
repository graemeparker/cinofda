drop table if exists segment;
create table segment (
 id int not null primary key,
 parent_id int,
 level_1 varchar(255) not null,
 level_2 varchar(255),
 level_3 varchar(255),
 level_4 varchar(255), 
 cpm decimal(4,2),
 definition varchar(512)
);

load data infile '/tmp/lotame-standard.csv' into table segment
  fields terminated by ',' enclosed by '"'
  lines terminated by '\r'
  ignore 1 lines
  (@var_id, @var_parent_id, level_1, level_2, level_3, @var_cpm, definition) 
  set id = substring(@var_id, 2),
    parent_id = substring(@var_parent_id, 2),
    cpm = substring(trim(@var_cpm), 2);

load data infile '/tmp/lotame-b2b.csv' into table segment
  fields terminated by ',' enclosed by '"'
  lines terminated by '\r'
  ignore 1 lines
  (@var_id, @var_parent_id, level_1, level_2, level_3, @var_cpm, definition) 
  set id = substring(@var_id, 2),
    parent_id = substring(@var_parent_id, 2),
    cpm = substring(trim(@var_cpm), 2);

load data infile '/tmp/lotame-cpg.csv' into table segment
  fields terminated by ',' enclosed by '"'
  lines terminated by '\r'
  ignore 1 lines
  (@var_id, @var_parent_id, level_1, level_2, level_3, @var_cpm, definition) 
  set id = substring(@var_id, 2),
    parent_id = substring(@var_parent_id, 2),
    cpm = substring(trim(@var_cpm), 2);

load data infile '/tmp/lotame-international.csv' into table segment
  fields terminated by ',' enclosed by '"'
  lines terminated by '\r'
  ignore 1 lines
  (@var_id, @var_parent_id, level_1, level_2, level_3, level_4, @var_cpm, definition) 
  set id = substring(@var_id, 2),
    parent_id = substring(@var_parent_id, 2),
    cpm = substring(trim(@var_cpm), 2);

load data infile '/tmp/lotame-android.csv' into table segment
  fields terminated by ',' enclosed by '"'
  lines terminated by '\r'
  ignore 1 lines
  (@var_id, @var_parent_id, level_1, level_2, level_3, @var_cpm, definition) 
  set id = substring(@var_id, 2),
    parent_id = substring(@var_parent_id, 2),
    cpm = substring(trim(@var_cpm), 2);

load data infile '/tmp/lotame-ocr-vce.csv' into table segment
  fields terminated by ',' enclosed by '"'
  lines terminated by '\r'
  ignore 1 lines
  (@var_id, @var_parent_id, level_1, level_2, level_3, @var_uniques, @var_cpm, definition) 
  set id = substring(@var_id, 2),
    parent_id = substring(@var_parent_id, 2),
    cpm = substring(trim(@var_cpm), 2);

