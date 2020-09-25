drop function add_movie;

DELIMITER $$
CREATE function add_movie ( title1 VARCHAR(100), 
							year1 INT, 
							director1 VARCHAR(100),
                            genre1 VARCHAR(32),
                            star1  VARCHAR(100))
RETURNS VARCHAR(100)
deterministic
BEGIN
	DECLARE genreId1 INT;
    DECLARE starId1 VARCHAR(10);
    DECLARE movieId1 VARCHAR(10);
    DECLARE id_num INT;
    DECLARE ans VARCHAR(100);
    IF ( (select count(*) from movies where title = title1 and year = year1 and director = director1) > 0) THEN
		RETURN 'Duplicated movie';
	ELSE
		select max(id) into movieId1 from movies;
        select SUBSTRING(movieId1, 3, 7) into movieId1;
		select CONVERT(movieId1, UNSIGNED) into id_num;
        SET id_num = id_num + 1;
		select CONVERT(id_num, CHAR) into movieId1;
        select CONCAT("tt", movieId1) into movieId1;
	    insert into movies (id,title,year,director) values (movieId1,title1,year1,director1);
	END IF;
    
    IF ( (select count(*) from genres where name=genre1) = 0) THEN
		select max(id+1) into genreId1 from genres;
        insert into genres (id,name) values (genreId1,genre1);
	ELSE
		select id into genreId1 from genres where name=genre1;
	END IF;
    insert into genres_in_movies (genreId,movieId) values (genreId1,movieId1);
    
	IF ( (select count(*) from stars where name=star1) = 0) THEN
		select max(id) into starId1 from stars;
		select SUBSTRING(starId1, 3, 7) into starId1;
		select CONVERT(starId1, UNSIGNED) into id_num;
        SET id_num = id_num + 1;
        select CONVERT(id_num, CHAR) into starId1;
        select CONCAT("nm", starId1) into starId1;
        insert into stars (id,name) values (starId1,star1);
	ELSE 
		select id into starId1 from stars where name=star1 limit 1;
	END IF;
    insert into stars_in_movies (starId,movieId) values (starId1,movieId1);
    insert into ratings (movieId,rating,numVotes) values (movieId1,0,0);
    
    select CONCAT("Movie added<br>Id: ",movieId1,"<br>Genre id: ",genreId1,"<br>Star id: ",starId1) into ans;
	RETURN ans;
END
$$    

DELIMITER ;

