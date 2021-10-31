# jupiter
A recommendation engine for Twitch

* Designed and built a full-stack web application for users to search twitch resources (stream/video/clip) and get recommendations. 

* Built a web page with rich + user friendly experience using React and Ant Design

* Implemented RESTful APIs using Java servlets, retrieved real Twitch resources using Twitch API and stored data in MySQL.

* Support login/logout and favorite collection.

* Explored multiple recommendation algorithms and extracted game information from Twitch resources to implement a Content-based algorithm.

* Deployed the service to AWS EC2 for better stability.

--- 
#### Load testing:

Apache JMeter is used to conduct a load testing on this project.

![image_without_connection_pool](https://cdn.jsdelivr.net/gh/lichever/pictureBedForNormalUse@main/uPic/image-20210708004453595_2021_07_08_17_29_19.png  "result with no connection pool")



The bottleneck is the connection to the database, and thus, the MySQL JDBC connection pool is used to improve the performance of this project. As can be seen, the throughput has a huge increase by using the connection pool.

![image_with_connection pool](https://cdn.jsdelivr.net/gh/lichever/pictureBedForNormalUse@main/uPic/image-20210708141911048_2021_07_08_14_19_13.png "result with connection pool")

--- 
#### Demo1: register
<img src="https://github.com/lichever/pictureBedForNormalUse/blob/main/gif/demo1_register.gif" width=650 height=360 />

#### Demo2: log in
<img src="https://github.com/lichever/pictureBedForNormalUse/blob/main/gif/demo2_login_popular.gif" width=650 height=360 />

#### Demo3: get popular games
<img src="https://github.com/lichever/pictureBedForNormalUse/blob/main/gif/demo3_popular.gif" width=650 height=360 />

#### Demo4: custom search
<img src="https://github.com/lichever/pictureBedForNormalUse/blob/main/gif/demo4_search.gif" width=650 height=360 />

#### Demo5: my favorite
<img src="https://github.com/lichever/pictureBedForNormalUse/blob/main/gif/demo5_fav.gif" width=650 height=360 />

#### Demo6: recommendation according to users' favorite records
<img src="https://github.com/lichever/pictureBedForNormalUse/blob/main/gif/demo6_recom.gif" width=650 height=360 />

