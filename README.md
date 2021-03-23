# Memory_Management
Simulation of 3 basic architectures of Memory Management in java, including Variable-Size Partitioning, Paging, and Segmentation.

The purpose of this project is write a simulation that explores the effects of limited memory and memory management
policies. The simulator will read policy information and the characteristics of the workload from input files and then will
simulate the execution of the processes as well as decisions of Memory Manager (MM). The simulator will generate an output file
with the trace of important “events,” as well as the memory map and the input queue status after each event. At the end of the 
simulation, the program will also print the average turnaround time per process.

The simulator will prompt the user for the size of the memory and then the memory management policy (and associated parameters) information. 
  The Memory Size (an integer) denotes the capacity of the main memory in the simulation (it can be interpreted as a multiple of Kbytes). 
  The Memory Management Policy can be:
    1. VSP: Variable-Size Partitioning (Contiguous Allocation). In this case, there will also be a policy parameter can be either 1, 2 or 3, encoding the algorithm used 
       for choosing among eligible “memory holes” when moving a process to the memory (1: First-Fit, 2: Best-Fit, 3: Worst-Fit).
    2. PAG: Paging. In this case, the policy parameter will denote the page size (consequently, also the frame size). We assume that the Memory Size will always be
       a multiple of the page (hence, frame) size.
    3. SEG: Segmentation. In this case, the policy parameter can be either 1, 2 or 3, denoting the algorithm used for choosing among eligible “memory holes” when moving 
       individual segments to the memory (1: First-Fit, 2: Best-Fit, 3: Worst-Fit).


The events displayed by the simulator that modify the memory contents and the input queue include:
   - Process arrival
   - Process admission to the main memory
   - Process completion


Since the memory is limited, there is no guarantee that a process will be admitted to the memory as soon as it arrives; thus it may have to wait until the system can
  accommodate its memory requirements. The lifetime in memory information for a given process defines how long the process will run ONCE IT HAS BEEN GIVEN SPACE IN MAIN 
  MEMORY. If a process is submitted to the system at time = 100 with Lifetime in Memory = 2000, but isn’t admitted to the memory until time = 1500, then it will complete 
  at time = 1500 + Lifetime in Memory = 3500. The memory space for a process will be freed by the memory manager when it completes.
   
