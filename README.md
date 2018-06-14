# RISCV-ISA-Emulator

An emulator that is able to execute a subset of the RISC-V ISA. Provides the machinery to decode and execute a couple dozen RISC-V instructions.

Here is a [RISCV](http://inst.eecs.berkeley.edu/~cs61c/fa17/img/riscvcard.pdf) Reference data sheet that provides all relevant information in regards to the RISCV machine language.

The instruction set that the emulator handles is listed below.

![P1](https://github.com/sameerkhanna786/RISCV-ISA-Emulator/blob/master/Pictures/P1.png)
![P2](https://github.com/sameerkhanna786/RISCV-ISA-Emulator/blob/master/Pictures/P2.png)

# Framework

The framework provided operates by doing the following.

It reads the program's machine code into the simulated memory (starting at address 0x01000). The program to "execute" is passed as a command line parameter. Each program is given 1 MiB of memory and is byte-addressable.
It initializes all 32 RISC-V registers to 0 and sets the program counter (PC) to 0x01000. The only exceptions to the initial initializations are the stack pointer (set to 0xEFFFF) and the global pointer (set to 0x03000). In the context of the emulator, the global pointer will refer to the static portion of our memory. The registers and Program Counter are managed by the Processor struct defined in types.h.
It sets flags that govern how the program interacts with the user. Depending on the options specified on the command line, the simulator will either show a dissassembly dump (-d) of the program on the command line, or it will execute the program. More information on the command line options is below.
It then enters the main simulation loop, which simply executes a single instruction repeatedly until the simulation is complete. Executing an instruction performs the following tasks:

It fetches an instruction from memory, using the PC as the address.
It examines the opcode/funct3 to determine what instruction was fetched.
It executes the instruction and updates the PC.
The framework supports a handful of command-line options:

-i runs the simulator in interactive mode, in which the simulator executes an instruction each time the Enter key is pressed. The disassembly of each executed instruction is printed.
-t runs the simulator in tracing mode, in which each instruction executed is printed.
-r instructs the simulator to print the contents of all 32 registers after each instruction is executed. This option is most useful when combined with the -i flag.
-d instructs the simulator to disassemble the entire program, then quit before executing.


