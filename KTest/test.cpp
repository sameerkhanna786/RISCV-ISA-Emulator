// Auto grading code for Proj2-1.
// Ver. Hive Machine
// Author: Li Qin (kroulis@berkeley.edu)
#include<iostream>
#include<fstream>
#include<cstring>
#include<cstdlib>
#include<string>
using namespace std;

bool FullTest;
bool GenTest;

int main(int args, char **argv){
  if (args == 3) {
    FullTest = atoi(argv[1]) > 0 ? true : false;
    GenTest = atoi(argv[2]) > 0? true : false;
  }
  ifstream config("conf.txt");
  int count;
  config>>count;
  string str = "";
  string final = "";
  int fail_count = 0;
  system("cd .. && make && cd KTest");
  system("chmod +x diff.sh");
  for (int i = 0; i < count; i++) {
    string op = "./diff.sh code" ;
	op += to_string(i);
	op += ".txt ref";
	op += to_string(i);
	op += ".txt > resultT.txt";
    cout<<"TEST " << i <<" ----------------------------------" <<endl;
    //cerr<<op<<endl;
    system(op.c_str());
    ifstream result("resultT.txt");
    bool start;
    bool flag = false;
    bool succeed_flag = false;
    while (getline(result,str)) {
      if(str.substr(0,8) == "Starting") {
      	start = true;
	  }
	  else if(start && str.substr(0,6) != "End of") {
	  	flag = true;
	  } else if(str.substr(0,6) == "End of") {
	  	start = false;
	  	succeed_flag = true;
	  }
      cout << str <<endl;
    }
    if(start || flag || !succeed_flag) {
    	cout<<">>>>>>>>> FAILED TEST" << endl;
    	fail_count ++;
	}
    result.close();
  }

  if (GenTest) {
    system("javac gen.java");
    for (int i = 0; i < 6; i++) {
      system("rm codegen.txt");
      system("rm refgen.txt");
      cout<<"GENTEST " << i << "-------------------------------" <<endl;
      cout<<"Start generate test case..." <<endl;
      int gencode = 1 << i;
      if (FullTest) {
        string opt = "java gen ";
        opt += to_string(gencode);
        opt += " ";
        opt += to_string(63);
        system(opt.c_str());
      } else {
        string opt = "java gen ";
        opt += to_string(gencode);
        opt += " ";
        opt += to_string(0);
        system(opt.c_str());
      }
      ifstream testcase("refgen.txt");
      if (!testcase.good()) {
        cout<<">>>>>>>>> GEN failed. Stop doing random test." << endl;
        testcase.close();
        break;
      }
      testcase.close();
      cout<<"case generated. Started testing..." <<endl;
      string op = "./diff.sh codegen.txt refgen.txt > resultT.txt" ;
      count ++;
      //cerr<<op<<endl;
      system(op.c_str());
      ifstream result("resultT.txt");
      bool start;
      bool flag = false;
      bool succeed_flag = false;
      while (getline(result,str)) {
        if(str.substr(0,8) == "Starting") {
        	start = true;
          cout << str <<endl;
  	  }
  	  else if(start && str.substr(0,6) != "End of") {
        if(!flag) {
          cout<<"Difference detected..." <<endl;
        }
        flag = true;
  	  } else if(str.substr(0,6) == "End of") {
  	  	start = false;
  	  	succeed_flag = true;
        cout << str <<endl;
  	  }
      }
      if(start || flag || !succeed_flag) {
      	cout<<">>>>>>>>> FAILED TEST" << endl;
      	fail_count ++;
      }
      result.close();

    }
  }
  cout<<"---------------------------------" <<endl;
  cout<<"## Passed: " << count-fail_count << " Failed: "<<fail_count << endl;
  system("rm resultT.txt");
  system("rm output.txt");
  return 0;
}
