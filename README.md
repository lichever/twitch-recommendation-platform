# jupiter
A recommendation engine for Twitch

* Designed and built a full-stack web application for users to search twitch resources (stream/video/clip) and get recommendations. 

* Built a web page with rich + user friendly experience using React and Ant Design

* Implemented RESTful APIs using Java servlets, retrieved real Twitch resources using Twitch API and stored data in MySQL.

* Support login/logout and favorite collection.

* Explored multiple recommendation algorithms and extracted game information from Twitch resources to implement a Content-based algorithm.

* Deployed the service to AWS EC2 for better stability.

> URL of demo: http://3.22.234.83/

--- 
#### Load testing:

Apache JMeter is used to conduct a load testing on this project.

![image_without_connection_pool](/Users/shawnzhang/Library/Application Support/typora-user-images/image-20210708004453595.png  "result with no connection pool")



The bottleneck is the connection to the database, and thus, the MySQL JDBC connection pool is used to improve the performance of this project. As can be seen, the throughput has a huge increase by using the connection pool.

![image_with_connection pool](https://cdn.jsdelivr.net/gh/lichever/pictureBedForNormalUse@main/uPic/image-20210708141911048_2021_07_08_14_19_13.png "result with connection pool")

