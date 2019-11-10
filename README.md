This is the backend of a web application delevoped as part of a course at TU-Berlin.
The specification was given to us by our professor. Our job was to first create the frontend, with the backend provided by the TU. And then create the backend aswell, using our frontend to test that everything is working as intended.

For part one we had 4.5 weeks to complete our work and for part 2 we had 2.5 weeks.

For this website to work, a server has to be online. At the very top of /js/script.js one can change the ip adress of the backend server.

We use Java with Spring Boot, and a MySQL database running in a Docker container.
The Docker one-liner for setting up the database is:
"docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=ThePass -e MYSQL_DATABASE=chatdb mysql"

Once you've set up the database, you should be able to start the server.

When the server is started, you can open the frontend website available  here https://github.com/flpeters/anwesys-chat-website-frontend and start chatting.