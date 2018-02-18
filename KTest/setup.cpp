/*
Patch note:
1.2 Fixed infinity loop.
*/

#include<cstdio>
#include<iostream>
#include<fstream>
#include<cstdlib>
#include<cstring>
#include<string.h>
using namespace std;

void setup() {
	//download index file
	cout<<"Downloading Index File..."<<endl;
	system("rm index.txt");
	system("wget https://www.kroulisworld.com/programs/61cproj/hive/1/index.txt  --no-check-certificate");
	ifstream index("index.txt");
	if(!index.good()) {
		cout<<"Failed to download index file. Setup Interrupted."<<endl;
		index.close();
		return;
	}
	cout<<"Start to downloading essential files..." <<endl;
	while(!index.eof()) {
		string line;
		getline(index,line);
		string rmop = "rm -f " + line;
		system(rmop.c_str());
		string op = "wget https://www.kroulisworld.com/programs/61cproj/hive/1/"+ line + " --no-check-certificate";
		system(op.c_str());
		ifstream test(line.c_str());
		if(!test.good()) {
			cout<<"Failed to download " << line << endl;
		}
		test.close();
	}
	index.close();
	cout<<"Start to build the testing script..." << endl;
	ifstream test("test.cpp");
	if(!test.good()) {
		cout<<"Testing script does not exist. Setup Interrupted." << endl;
		return;
	}
	test.close();
	system("g++ -std=c++0x -o test test.cpp");
	cout<<"Setup Done..."<<endl;
}

void run(int mode) {
	ifstream test("test");
	if(!test.good()) {
		cout<<"Testing script does not exist. Testing Interrupted." << endl;
		return;
	}
	switch(mode) {
		case 2:
			system("./test 1 1");
			break;
		case 1:
			system("./test 0 1");
			break;
		case 0:
			system("./test 0 0");
			break;
		default:
			system("./test");
	}
}

int main() {
	cout<<"©°--------------------------------©´"<<endl;
	cout<<"|      CS61C Project Checker      | " <<endl;
	cout<<"|    Ver: 1.2 for hive machine    | " <<endl;
	cout<<"|     Proj 2 - RISC-V Part 1      | " <<endl;
	cout<<"|By: Li Qin (kroulis@berkeley.edu)| " <<endl;
	cout<<"©¸--------------------------------©¼"<<endl;
	string choice = "-1";
	while(choice != "5") {
		cout<<"# Menu:" <<endl;
		cout<<"1. Setup project checker for proj2 part 1." <<endl;
		cout<<"2. Run checker for FULL TEST. " <<endl;
		cout<<"3. Run checker for PARTIAL TEST." <<endl;
		cout<<"4. Run checker for PARTIAL TEST w/o GEN." <<endl;
		cout<<"5. Exit." <<endl;
		cout<<"61cprojchk> ";
		cin>>choice;
		switch(choice[0]) {
			case '1':
				setup();
				break;
			case '2':
				run(2);
				break;
			case '3':
				run(1);
				break;
			case '4':
				run(0);
				break;
			case '5':
				break;
			default:
				cout<<"Unknown commend." <<endl;
		}
	}
	return 0;
}
