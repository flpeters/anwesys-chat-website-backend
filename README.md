# Chat Website Backend
> Delevoped as part of a course at TU-Berlin  
> The specification was given to us by our professor.  

Our job was to first create the [frontend](https://github.com/flpeters/anwesys-chat-website-frontend), with the __backend__ provided by the TU. And then create the backend as well, using our frontend to test that everything is working as intended.  
For frontend we had 4.5 weeks to complete our work and for the backend we had 2.5 weeks.

# How to use
For this Project to work, both the [frontend](https://github.com/flpeters/anwesys-chat-website-frontend) and the __backend__ are needed.  
At the very top of /js/script.js in the frontend, one can change the ip adress of the backend server.

- We use Java with Spring Boot, and a MySQL database running in a Docker container.
- The Docker one-liner for setting up the database is: `docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=ThePass -e MYSQL_DATABASE=chatdb mysql`
- This repo is in the form of an [intelliJ](https://www.jetbrains.com/idea/) project

Once you've set up the database, you should be able to start the server.

When the server is started, you can open the frontend website available here https://github.com/flpeters/anwesys-chat-website-frontend and start chatting.
