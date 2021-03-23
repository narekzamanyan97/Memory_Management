import java.util.Scanner;

public class Memory_Main {
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

  public static void main(String[] args) throws Exception {
    Scanner input = new Scanner(System.in);
    int select = 0;
    int memory_size;
    int mmp;
    int fit_algorithm;
    int page_size;
    String file_name;

    do{
      System.out.println("Select 1 to run the simulation, 0 to quit: ");
      select = input.nextInt();
      input.nextLine();
      if(select == 1) {

        System.out.print("Enter the file name: ");
        file_name = input.nextLine();
        System.out.print("Memory Size: ");
        memory_size = input.nextInt();
        input.nextLine();
        System.out.print("Memory Management Policy (1- VSP, 2 - PAG, 3 - SEG): ");
        mmp = input.nextInt();
        input.nextLine();
        if(mmp == PAG) {
          System.out.print("Page Size: ");
          page_size = input.nextInt();
          input.nextLine();
          Memory_Management_Unit mmu = new Memory_Management_Unit(memory_size, file_name, PAG, page_size);
          mmu.simulation();
        }
        else if(mmp == VSP || mmp == SEG){
          System.out.print("Fit algorithm (1-first-fit, 2- best-fit, 3- worst-fit): ");
          fit_algorithm = input.nextInt();
          input.nextLine();
          Memory_Management_Unit mmu = new Memory_Management_Unit(memory_size, file_name, mmp, fit_algorithm);
          mmu.simulation();
        }
      }
      //mmu.file_reader();
      //mmu.test_file_reader();\

    }while(select == 1);

  }

}
