import java.util.Queue;
import java.util.LinkedList;
import java.io.File;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Memory_Management_Unit {

  static final int MAX_PROCESSES = 20;
  static final int MAX_MEMORY_SIZE = 30000;
  static final int MAX_TIME = 100000;

  //Memory Management Policy constants
  static final int VSP = 1;
  static final int PAG = 2;
  static final int SEG = 3;

  //algorithm used for VSP and SEG
  static final int FIRST_FIT = 1;
  static final int BEST_FIT = 2;
  static final int WORST_FIT = 3;

  static final boolean MOVE_FAILED = false;


  public int clock;

  public int number_of_processes;

  ArrayList<Integer> input_queue;

  Process[] processes;

  Memory_Map memory_map;

  int policy;

  public File file;
  public Scanner file_reader;

  public Memory_Management_Unit(int memorySize, String file_name, int policy, int algorithm_or_pag_size) {
    memory_map = new Memory_Map(memorySize, policy, algorithm_or_pag_size);
    this.policy = policy;
    processes = new Process[MAX_PROCESSES];
    for(int i = 0; i < MAX_PROCESSES; i++) {
      processes[i] = new Process();
    }
    input_queue = new ArrayList<>();

    clock = 0;

    try {
      file = new File(file_name);
      file_reader = new Scanner(file);
      file_reader();
    }
    catch(FileNotFoundException e) {
      System.out.println("An Error occurred");
      e.printStackTrace();
    }
  }

  /*
    ****************************************************************************
                                file_reader
    ****************************************************************************
  */

  //@File_Reader reads the given file and sets the variables according to the
  // contents of the file
  public void file_reader(){

    String[] arrival_and_lifetime_string = new String[2];
    List<String> temp_segments = new ArrayList<String>();
    String temp_str = new String();
    if(file_reader.hasNextLine()) {
      temp_str = file_reader.nextLine();
    }
    number_of_processes = Integer.parseInt(temp_str);
    //initializing the total_process_size of the process to 0
    int temp_total_process_size = 0;

    for(int i = 0; i < number_of_processes && file_reader.hasNextLine(); i++) {
      //READING THE 1ST LINE: PROCESS ID
      if(file_reader.hasNextLine()) {
        //can ignore this, because the index + 1 already represents the p_id
        processes[i].set_process_id(Integer.parseInt(file_reader.nextLine().trim()));
      }

      //READING THE 2ND LINE: ARRIVAL AND LIFETIME OF A PROCESS
      if(file_reader.hasNextLine()) {
        temp_str = file_reader.nextLine();
      }
      //splitting the arrival and lifetime into elements in an array
      arrival_and_lifetime_string = temp_str.split(" ");
      //parsing the string read from the file into an integer
      //just parse it straght to the array
      processes[i].set_arrival_time(Integer.parseInt(arrival_and_lifetime_string[0]));
      processes[i].set_lifetime(Integer.parseInt(arrival_and_lifetime_string[1]));

      //READING THE 3RD LINE: NUMBER OF SEGMENTS AND SEGMENT SIZES

      temp_str = file_reader.nextLine();
      temp_segments = Arrays.asList(temp_str.split(" "));
      
      for(int j = 1; j < temp_segments.size(); j++) {
          temp_total_process_size += Integer.parseInt(temp_segments.get(j));
          processes[i].add_segment(Integer.parseInt(temp_segments.get(j)));
      }
      
      //READING THE 4TH LINE: SPACE
      if(file_reader.hasNextLine()) {
        file_reader.nextLine();
      }
    }

    file_reader.close();
  }

  /*
    ****************************************************************************
                                test_file_reader
    ****************************************************************************
  */

  public void test_file_reader(){
    System.out.println("Number of processes: " + number_of_processes);
    for(int i = 0; i < number_of_processes; i++) {
      System.out.println("Process ID: " + processes[i].get_process_id());
      System.out.println("Arrival: " + processes[i].get_arrival_time());
      System.out.println("Lifetime: " + processes[i].get_lifetime());
      System.out.print(processes[i].get_number_of_segments() + " ");
      for(int j = 0; j < processes[i].get_number_of_segments(); j++) {
        System.out.print(processes[i].get_segment_size(j) + " ");
      }
      System.out.println("  Total = " + processes[i].get_total_process_size());

    }
  }



  /*
    ****************************************************************************
                                      VSP simulation
    ****************************************************************************
  */
  public void simulation() {
    int number_of_arrived_processes = 0;
    int number_of_completed_processes = 0;
    int top_of_queue;

    boolean[] is_process_in_memory = new boolean[MAX_PROCESSES];

    //a boolean to help print the time only once when processes complete
    boolean already_printed_time = false;

    int top_of_input_queue = 0;

    double total_turnaround_time = 0;
    double average_turnaround_time = 0;

    while(number_of_completed_processes < this.number_of_processes) {
      int processes_checked = 0;
      /*************************************************************
                    check if any process arives
      *************************************************************/
      // processess arrive
      if(this.processes[number_of_arrived_processes].get_arrival_time() == clock) {
        System.out.println("t = " + clock + " ");
        while(this.processes[number_of_arrived_processes].get_arrival_time() == clock) {
          System.out.println("    Process " + processes[number_of_arrived_processes].get_process_id() + " arrives");

          //add the arriving process to the input queue
          this.input_queue.add(top_of_input_queue,
                  processes[number_of_arrived_processes].get_process_id());

          top_of_input_queue++;

          //print the input queue
          System.out.print("    Input Queue:");
          System.out.print("[");
          for(int q = 0; q < this.input_queue.size(); q++) {
            System.out.print(" " + this.input_queue.get(q));
          }
          System.out.println(" ]");

          //print memory map
          this.memory_map.print_memory_map();

          number_of_arrived_processes++;
        }
      }

      /*************************************************************
                    check if any process completes
      *************************************************************/
      for(int i = 0; i < number_of_processes; i++) {
        //first, check if the process is in memory
        if(is_process_in_memory[i] == true) {
          //if it is time for the process in memory to complete
          if(clock == processes[i].get_process_memory_entrance_time() + processes[i].get_lifetime()) {
            if(already_printed_time == false) {
              System.out.println("t = " + clock + " ");
              already_printed_time = true;
            }
            total_turnaround_time += clock - processes[i].get_arrival_time();
            System.out.println("    Process " + processes[i].get_process_id() + " completes");
            memory_map.remove_from_memory(processes[i].get_process_id());
            is_process_in_memory[i] = false;
            number_of_completed_processes++;
            memory_map.print_memory_map();
          }
        }
      }

      already_printed_time = false;


      /*************************************************************
                    move processes from input_queue to memory
      *************************************************************/
      if(input_queue.size() > 0) {
        processes_checked = 0;

        top_of_queue = input_queue.get(processes_checked);

        /*
          check:
            1) if the memory has large enough hole to accomodate the top_of_queue
                process
            2) if the input queue is not empty
        */
        if(policy == VSP) {
          while(input_queue.size() > 0 && processes_checked < input_queue.size()) {

            top_of_queue = input_queue.get(processes_checked);

            if(this.memory_map.get_available_hole_pointer(processes[top_of_queue - 1]) != -1) {

              System.out.println("    MM moves process " + processes[top_of_queue - 1].get_process_id() + " to memory");
              memory_map.move_to_memory(processes[top_of_queue - 1]);
              processes[top_of_queue - 1].set_memory_entrance_time(clock);
              //now the process top_of_queue is in memory
              is_process_in_memory[top_of_queue - 1] = true;
              input_queue.remove(processes_checked);
              top_of_input_queue--;
              memory_map.print_memory_map();
            }
            else {
              processes_checked++;
            }
          }
        }
        else if(policy == PAG) {
          while(input_queue.size() > 0 && processes_checked < input_queue.size()) {

            top_of_queue = input_queue.get(processes_checked);

            if(memory_map.move_to_memory(processes[top_of_queue - 1]) == true) {

              System.out.println("    MM moves process " + processes[top_of_queue - 1].get_process_id()
                      + " to memory");
              processes[top_of_queue - 1].set_memory_entrance_time(clock);
              //now the process top_of_queue is in memory
              is_process_in_memory[top_of_queue - 1] = true;
              input_queue.remove(processes_checked);
              top_of_input_queue--;
              memory_map.print_memory_map();
            }
            else {
              processes_checked++;
            }
          }
        }
        // policy == SEG
        else{
          while(input_queue.size() > 0 && processes_checked < input_queue.size()) {

            top_of_queue = input_queue.get(processes_checked);
            //System.out.println("processes_checked = " + processes_checked);


            if(memory_map.move_to_memory(processes[top_of_queue - 1]) == true) {
              System.out.println("    MM moves process " + processes[top_of_queue - 1].get_process_id()
                      + " to memory");


              processes[top_of_queue - 1].set_memory_entrance_time(clock);
              //now the process top_of_queue is in memory
              is_process_in_memory[top_of_queue - 1] = true;
              input_queue.remove(processes_checked);
              top_of_input_queue--;

              //print the input queue
              System.out.print("    Input Queue:");
              System.out.print("[");
              for(int q = 0; q < this.input_queue.size(); q++) {
                System.out.print(" " + this.input_queue.get(q));
              }
              System.out.println(" ]");

              memory_map.print_memory_map();
            }
            else {
              processes_checked++;
            }
          }
        }
      }

      clock++;

    }

    average_turnaround_time = total_turnaround_time / number_of_processes;
    System.out.printf("Average Turnaround Time: %.2f", average_turnaround_time);
    System.out.println();
  }
}
