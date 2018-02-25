mkdir KTest
cd KTest
rm setup.cpp
wget https://www.kroulisworld.com/programs/61cproj/hive/2/setup.cpp --no-check-certificate
g++ -o setup setup.cpp
./setup
