CREATE DATABASE IF NOT EXISTS moviedb;
USE moviedb;
CREATE TABLE IF NOT EXISTS movies(
			id varchar(10) not null,
			title varchar(100) not null,
			year integer not null,
			director varchar(100) not null,
			PRIMARY KEY(id)
);
CREATE TABLE IF NOT EXISTS stars(
			id varchar(10) not null,
			name varchar(100) not null,
			birthYear integer,
			PRIMARY KEY(id)
);
CREATE TABLE IF NOT EXISTS stars_in_movies(
			starId varchar(10) not null,
			movieId varchar(10) not null,
			FOREIGN KEY (starId) REFERENCES stars(id),
			FOREIGN KEY (movieId) REFERENCES movies(id)
);
CREATE TABLE IF NOT EXISTS genres(
			id integer not null AUTO_INCREMENT,
			name varchar(32) not null,
            PRIMARY KEY(id)
);
CREATE TABLE IF NOT EXISTS genres_in_movies(
			genreId integer not null,
            movieId varchar(10) not null,
			FOREIGN KEY (genreId) REFERENCES genres(id),
            FOREIGN KEY (movieId) REFERENCES movies(id)
);
CREATE TABLE IF NOT EXISTS creditcards(
			id varchar(20) not null,
            firstName varchar(50) not null,
            lastName varchar(50) not null,
            expiration date not null,
			PRIMARY KEY(id)
);
CREATE TABLE IF NOT EXISTS customers(
			id integer not null AUTO_INCREMENT,
            firstName varchar(50) not null,
            lastName varchar(50) not null,
            ccId varchar(20) not null,
            address varchar(200) not null,
            email varchar(50) not null,
            password varchar(20) not null,
            PRIMARY KEY(id),
            FOREIGN KEY (ccId) REFERENCES creditcards(id)
);
CREATE TABLE IF NOT EXISTS sales(
			id integer not null AUTO_INCREMENT,
            customerId integer not null,
			movieId varchar(10) not null,
            saleDate date not null,
            PRIMARY KEY(id),
            FOREIGN KEY (customerId) REFERENCES customers(id),
            FOREIGN KEY (movieId) REFERENCES movies(id)
);
CREATE TABLE IF NOT EXISTS ratings(
			movieId varchar(10) not null,
            rating float not null,
            numVotes integer not null,
            FOREIGN KEY (movieId) REFERENCES movies(id)
);
/*
CREATE TABLE IF NOT EXISTS customers_backup(
			id integer not null AUTO_INCREMENT,
            firstName varchar(50) not null,
            lastName varchar(50) not null,
            ccId varchar(20) not null,
            address varchar(200) not null,
            email varchar(50) not null,
            password varchar(128) not null,
            PRIMARY KEY(id),
            FOREIGN KEY (ccId) REFERENCES creditcards(id)
);

insert into customers_backup select * from customers;

CREATE TABLE IF NOT EXISTS employees (
            email varchar(50) primary key,
            password varchar(20) not null,
            fullname varchar(100)
);

insert into employees(email, password, fullname) values ("classta@email.edu", "classta", "TA CS122B");
*/

drop table if exists ft;
CREATE TABLE IF NOT EXISTS ft (
                              entryID varchar(10) primary key,
                              entry text,
                              FOREIGN KEY (entryID) REFERENCES movies(id),
                              FULLTEXT (entry));

INSERT INTO ft (select id,title from movies);
